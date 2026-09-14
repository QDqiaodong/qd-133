package com.qd.mashu.service;

import com.qd.mashu.dto.request.RiderLevelUpdateRequest;
import com.qd.mashu.dto.response.RiderResponse;
import com.qd.mashu.entity.LevelChangeLog;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.LevelChangeLogRepository;
import com.qd.mashu.repository.RiderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 骑手改级核心规则单元测试：幂等去重、必填校验、只升不降。
 */
@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private LevelChangeLogRepository levelChangeLogRepository;

    @Mock
    private TrainingStationService trainingStationService;

    @InjectMocks
    private RiderService riderService;

    private Rider riderAt(TrainingLevel level) {
        return Rider.builder()
                .id(1L)
                .riderCode("RD001")
                .riderName("张三")
                .currentLevel(level)
                .status(1)
                .build();
    }

    private RiderLevelUpdateRequest request(Integer newLevel, String reason, String operator) {
        return RiderLevelUpdateRequest.builder()
                .riderId(1L)
                .newLevel(newLevel)
                .changeReason(reason)
                .operator(operator)
                .build();
    }

    @Test
    void updateLevel_normal_upgrade_writes_one_log() {
        Rider rider = riderAt(TrainingLevel.LEVEL_1);
        when(riderRepository.findWithLockById(1L)).thenReturn(Optional.of(rider));
        when(riderRepository.save(any(Rider.class))).thenAnswer(inv -> inv.getArgument(0));

        RiderResponse response = riderService.updateLevel(request(2, "考核通过", "陈教练"));

        assertEquals(2, response.getCurrentLevel());
        verify(levelChangeLogRepository, times(1)).save(any(LevelChangeLog.class));
        verify(trainingStationService).revalidateStationsAfterLevelChange(rider);
    }

    @Test
    void updateLevel_same_level_twice_second_one_is_idempotent_no_extra_log() {
        Rider rider = riderAt(TrainingLevel.LEVEL_1);
        when(riderRepository.findWithLockById(1L)).thenReturn(Optional.of(rider));
        when(riderRepository.save(any(Rider.class))).thenAnswer(inv -> inv.getArgument(0));

        // 第一次：升到中级，记一条变更
        riderService.updateLevel(request(2, "考核通过", "陈教练"));
        // 网络卡顿连点的第二次：同一骑手同一新等级，直接返回当前状态，不再记一条
        RiderResponse second = riderService.updateLevel(request(2, "考核通过", "陈教练"));

        assertEquals(2, second.getCurrentLevel());
        verify(levelChangeLogRepository, times(1)).save(any(LevelChangeLog.class));
        verify(riderRepository, times(1)).save(any(Rider.class));
        verify(trainingStationService, times(1)).revalidateStationsAfterLevelChange(any(Rider.class));
    }

    @Test
    void updateLevel_missing_reason_or_operator_rejected() {
        IllegalArgumentException noReason = assertThrows(IllegalArgumentException.class,
                () -> riderService.updateLevel(request(2, "  ", "陈教练")));
        IllegalArgumentException noOperator = assertThrows(IllegalArgumentException.class,
                () -> riderService.updateLevel(request(2, "考核通过", null)));

        assertTrue(noReason.getMessage().contains("还没写全"));
        assertTrue(noOperator.getMessage().contains("还没写全"));
        verifyNoInteractions(riderRepository, levelChangeLogRepository, trainingStationService);
    }

    @Test
    void updateLevel_downgrade_rejected() {
        Rider rider = riderAt(TrainingLevel.LEVEL_3);
        when(riderRepository.findWithLockById(1L)).thenReturn(Optional.of(rider));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> riderService.updateLevel(request(2, "状态下滑", "陈教练")));

        assertTrue(ex.getMessage().contains("不能降级"));
        verify(levelChangeLogRepository, never()).save(any(LevelChangeLog.class));
    }
}
