package com.qd.mashu.service;

import com.qd.mashu.dto.request.DutyLogRequest;
import com.qd.mashu.dto.response.DutyLogResponse;
import com.qd.mashu.entity.DutyLog;
import com.qd.mashu.repository.DutyLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班记录服务：每天只写一条，当班教练、接班教练、今晚留意事项缺一不可。
 *
 * <p>记录落库持久化，可按日期翻查前几天的交接内容；duty_date 还有唯一约束兜底，
 * 即使重复请求同时到达，也不会在同一天插入两条记录。
 */
@Service
public class DutyLogService {

    private static final Logger logger = LoggerFactory.getLogger(DutyLogService.class);

    private static final int COACH_MAX_LENGTH = 50;
    private static final int NOTES_MAX_LENGTH = 1000;

    @Autowired
    private DutyLogRepository dutyLogRepository;

    /**
     * 提交值班记录：日期、当班教练、接班教练、今晚留意事项四项必填。
     */
    @Transactional
    public DutyLogResponse create(DutyLogRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("值班记录不能为空");
        }
        LocalDate dutyDate = request.getDutyDate();
        if (dutyDate == null) {
            throw new IllegalArgumentException("请选择值班日期");
        }

        String onDutyCoach = requireCoach(request.getOnDutyCoach(), "当班教练");
        String incomingCoach = requireCoach(request.getIncomingCoach(), "接班教练");

        if (request.getTonightNotes() == null || request.getTonightNotes().trim().isEmpty()) {
            throw new IllegalArgumentException("请写下今晚要留意的事");
        }
        String tonightNotes = request.getTonightNotes().trim();
        if (tonightNotes.length() > NOTES_MAX_LENGTH) {
            throw new IllegalArgumentException("今晚要留意的事不能超过" + NOTES_MAX_LENGTH + "字");
        }

        if (dutyLogRepository.existsByDutyDate(dutyDate)) {
            throw new IllegalArgumentException(dutyDate + " 的值班记录已经写过，一天只能写一条");
        }

        DutyLog dutyLog = DutyLog.builder()
                .dutyDate(dutyDate)
                .onDutyCoach(onDutyCoach)
                .incomingCoach(incomingCoach)
                .tonightNotes(tonightNotes)
                .build();

        dutyLog = dutyLogRepository.save(dutyLog);
        logger.info("Created duty log: date={} onDuty={} incoming={}",
                dutyDate, onDutyCoach, incomingCoach);
        return DutyLogResponse.fromEntity(dutyLog);
    }

    /**
     * 查询值班记录：不传日期查全部（日期倒序），传日期只查当天，便于翻回前几天。
     */
    public List<DutyLogResponse> list(LocalDate dutyDate) {
        List<DutyLog> logs = dutyDate == null
                ? dutyLogRepository.findAllByOrderByDutyDateDescIdDesc()
                : dutyLogRepository.findByDutyDateOrderByDutyDateDescIdDesc(dutyDate);
        return logs.stream()
                .map(DutyLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private String requireCoach(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("请填写" + fieldName);
        }
        String coach = value.trim();
        if (coach.length() > COACH_MAX_LENGTH) {
            throw new IllegalArgumentException(fieldName + "不能超过" + COACH_MAX_LENGTH + "字");
        }
        return coach;
    }
}
