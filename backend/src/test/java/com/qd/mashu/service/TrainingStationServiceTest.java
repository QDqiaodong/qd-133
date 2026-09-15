package com.qd.mashu.service;

import com.qd.mashu.dto.request.TrainingStationRequest;
import com.qd.mashu.dto.response.TrainingStationResponse;
import com.qd.mashu.dto.response.TrainingStationSummary;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.TrainingStation;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import com.qd.mashu.repository.RiderRepository;
import com.qd.mashu.repository.TrainingStationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 骑手档案停用与训练位绑定之间的规则单测：
 * 停用的人不能再绑上位；挂着停用骑手的位重开名单时按空闲投影（杆保留）。
 */
@ExtendWith(MockitoExtension.class)
class TrainingStationServiceTest {

    @Mock
    private TrainingStationRepository stationRepository;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private ObstacleEquipmentRepository equipmentRepository;

    @Mock
    private ObstacleEquipmentService equipmentService;

    @Mock
    private HeightRecheckService heightRecheckService;

    @InjectMocks
    private TrainingStationService stationService;

    private Rider rider(long id, String name, int status) {
        return Rider.builder()
                .id(id)
                .riderCode("RD00" + id)
                .riderName(name)
                .currentLevel(TrainingLevel.LEVEL_1)
                .status(status)
                .build();
    }

    private TrainingStation station(long id, String code, String name, Rider rider, ObstacleEquipment equipment) {
        return TrainingStation.builder()
                .id(id)
                .stationCode(code)
                .stationName(name)
                .rider(rider)
                .equipment(equipment)
                .status(1)
                .build();
    }

    private ObstacleEquipment pole() {
        return ObstacleEquipment.builder()
                .id(7L)
                .equipmentCode("EQ001")
                .equipmentName("初级障碍杆A组")
                .adaptLevel(TrainingLevel.LEVEL_1)
                .status(1)
                .build();
    }

    @Test
    void bind_inactive_rider_rejected() {
        Rider inactive = rider(1L, "张三", 0);
        when(stationRepository.findById(10L))
                .thenReturn(Optional.of(station(10L, "ST004", "训练位4号", null, null)));
        when(riderRepository.findWithLockById(1L)).thenReturn(Optional.of(inactive));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stationService.bindRiderAndEquipment(10L, 1L, 7L));

        // 下拉里点得到，提交过不去
        assertTrue(ex.getMessage().contains("已停用"));
        verify(equipmentRepository, never()).findById(any());
        verify(stationRepository, never()).save(any());
    }

    @Test
    void create_with_inactive_rider_rejected() {
        Rider inactive = rider(1L, "张三", 0);
        when(stationRepository.existsByStationCode("ST009")).thenReturn(false);
        when(riderRepository.findWithLockById(1L)).thenReturn(Optional.of(inactive));

        TrainingStationRequest req = TrainingStationRequest.builder()
                .stationCode("ST009")
                .stationName("新训练位")
                .riderId(1L)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stationService.create(req));

        assertTrue(ex.getMessage().contains("已停用"));
        verify(stationRepository, never()).save(any());
    }

    @Test
    void update_with_inactive_rider_rejected() {
        Rider inactive = rider(1L, "张三", 0);
        when(stationRepository.findById(10L))
                .thenReturn(Optional.of(station(10L, "ST004", "训练位4号", null, null)));
        when(riderRepository.findWithLockById(1L)).thenReturn(Optional.of(inactive));

        TrainingStationRequest req = TrainingStationRequest.builder()
                .stationCode("ST004")
                .stationName("训练位4号")
                .riderId(1L)
                .build();

        assertThrows(IllegalArgumentException.class, () -> stationService.update(10L, req));
        verify(stationRepository, never()).save(any());
    }

    @Test
    void list_station_with_inactive_rider_is_projected_free_but_keeps_pole() {
        Rider inactive = rider(1L, "张三", 0);
        ObstacleEquipment pole = pole();
        // 历史/异常脏数据：库里位上仍挂着已停用的人，杆还在位上
        TrainingStation dirty = station(10L, "ST001", "训练位1号", inactive, pole);
        Rider active = rider(2L, "李四", 1);
        TrainingStation occupied = station(11L, "ST002", "训练位2号", active, pole);
        when(stationRepository.findByStatus(1)).thenReturn(List.of(dirty, occupied));

        List<TrainingStationResponse> responses = stationService.listAll();

        TrainingStationResponse first = responses.get(0);
        // 关掉名单再打开，停用的人不再出现在占用名单里
        assertFalse(first.getOccupied());
        assertEquals("FREE", first.getOccupancyStatus());
        assertNull(first.getRider());
        // 杆可以留着
        assertNotNull(first.getEquipment());
        // 库里实体没被改动
        assertNotNull(dirty.getRider());

        assertTrue(responses.get(1).getOccupied());
        assertEquals("李四", responses.get(1).getRider().getRiderName());
    }

    @Test
    void summary_counts_inactive_rider_station_as_free() {
        Rider inactive = rider(1L, "张三", 0);
        Rider active = rider(2L, "李四", 1);
        when(stationRepository.findByStatus(1)).thenReturn(List.of(
                station(10L, "ST001", "训练位1号", inactive, pole()),
                station(11L, "ST002", "训练位2号", active, pole()),
                station(12L, "ST004", "训练位4号", null, null)));

        TrainingStationSummary summary = stationService.getSummary();

        assertEquals(3, summary.getTotal());
        assertEquals(1, summary.getOccupied());
        assertEquals(2, summary.getFree());
    }
}
