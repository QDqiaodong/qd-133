package com.qd.mashu.service;

import com.qd.mashu.dto.request.DutyLogRequest;
import com.qd.mashu.dto.response.DutyLogResponse;
import com.qd.mashu.entity.DutyLog;
import com.qd.mashu.repository.DutyLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 值班记录核心规则单元测试：四项必填、一天一条、可按日期翻查。
 */
@ExtendWith(MockitoExtension.class)
class DutyLogServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 15);

    @Mock
    private DutyLogRepository dutyLogRepository;

    @InjectMocks
    private DutyLogService dutyLogService;

    private DutyLogRequest request(LocalDate date, String onDuty, String incoming, String notes) {
        return DutyLogRequest.builder()
                .dutyDate(date)
                .onDutyCoach(onDuty)
                .incomingCoach(incoming)
                .tonightNotes(notes)
                .build();
    }

    private DutyLog entity(LocalDate date, String onDuty, String incoming, String notes) {
        return DutyLog.builder()
                .id(1L)
                .dutyDate(date)
                .onDutyCoach(onDuty)
                .incomingCoach(incoming)
                .tonightNotes(notes)
                .build();
    }

    @Test
    void create_complete_duty_log_saved() {
        when(dutyLogRepository.existsByDutyDate(TODAY)).thenReturn(false);
        when(dutyLogRepository.save(any(DutyLog.class))).thenAnswer(inv -> inv.getArgument(0));

        DutyLogResponse response = dutyLogService.create(request(
                TODAY, " 陈教练 ", "林教练", " 三号训练位栏杆松动，今晚安排维修 "));

        assertEquals(TODAY, response.getDutyDate());
        assertEquals("陈教练", response.getOnDutyCoach());
        assertEquals("林教练", response.getIncomingCoach());
        assertEquals("三号训练位栏杆松动，今晚安排维修", response.getTonightNotes());
        verify(dutyLogRepository, times(1)).save(any(DutyLog.class));
    }

    @Test
    void create_missing_any_required_field_rejected() {
        IllegalArgumentException noDate = assertThrows(IllegalArgumentException.class,
                () -> dutyLogService.create(request(null, "陈教练", "林教练", "器材要换")));
        IllegalArgumentException noOnDuty = assertThrows(IllegalArgumentException.class,
                () -> dutyLogService.create(request(TODAY, " ", "林教练", "器材要换")));
        IllegalArgumentException noIncoming = assertThrows(IllegalArgumentException.class,
                () -> dutyLogService.create(request(TODAY, "陈教练", "", "器材要换")));
        IllegalArgumentException noNotes = assertThrows(IllegalArgumentException.class,
                () -> dutyLogService.create(request(TODAY, "陈教练", "林教练", "  ")));

        assertTrue(noDate.getMessage().contains("值班日期"));
        assertTrue(noOnDuty.getMessage().contains("当班教练"));
        assertTrue(noIncoming.getMessage().contains("接班教练"));
        assertTrue(noNotes.getMessage().contains("今晚要留意"));
        verifyNoInteractions(dutyLogRepository);
    }

    @Test
    void create_duplicate_date_rejected_before_save() {
        when(dutyLogRepository.existsByDutyDate(TODAY)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> dutyLogService.create(request(TODAY, "陈教练", "林教练", "器材要换")));

        assertTrue(ex.getMessage().contains("一天只能写一条"));
        verify(dutyLogRepository, never()).save(any(DutyLog.class));
    }

    @Test
    void list_without_date_returns_all_records() {
        DutyLog yesterday = entity(TODAY.minusDays(1), "王教练", "陈教练", "昨日事项");
        DutyLog today = entity(TODAY, "陈教练", "林教练", "今日事项");
        when(dutyLogRepository.findAllByOrderByDutyDateDescIdDesc())
                .thenReturn(List.of(today, yesterday));

        List<DutyLogResponse> result = dutyLogService.list(null);

        assertEquals(2, result.size());
        assertEquals(TODAY, result.get(0).getDutyDate());
        verify(dutyLogRepository).findAllByOrderByDutyDateDescIdDesc();
        verify(dutyLogRepository, never()).findByDutyDateOrderByDutyDateDescIdDesc(any());
    }

    @Test
    void list_with_date_queries_that_day() {
        when(dutyLogRepository.findByDutyDateOrderByDutyDateDescIdDesc(TODAY))
                .thenReturn(List.of(entity(TODAY, "陈教练", "林教练", "今日事项")));

        List<DutyLogResponse> result = dutyLogService.list(TODAY);

        assertEquals(1, result.size());
        assertEquals(TODAY, result.get(0).getDutyDate());
        verify(dutyLogRepository).findByDutyDateOrderByDutyDateDescIdDesc(TODAY);
        verify(dutyLogRepository, never()).findAllByOrderByDutyDateDescIdDesc();
    }
}
