package com.qd.mashu.service;

import com.qd.mashu.dto.response.LevelChangeLogResponse;
import com.qd.mashu.entity.LevelChangeLog;
import com.qd.mashu.repository.LevelChangeLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LevelChangeLogService {

    @Autowired
    private LevelChangeLogRepository levelChangeLogRepository;

    public List<LevelChangeLogResponse> getLogsByRider(Long riderId) {
        return levelChangeLogRepository.findByRiderIdOrderByCreateTimeDesc(riderId).stream()
                .map(LevelChangeLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}