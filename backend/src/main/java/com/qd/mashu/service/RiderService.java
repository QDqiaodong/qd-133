package com.qd.mashu.service;

import com.qd.mashu.dto.request.RiderLevelUpdateRequest;
import com.qd.mashu.dto.request.RiderRequest;
import com.qd.mashu.dto.response.RiderResponse;
import com.qd.mashu.entity.LevelChangeLog;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.LevelChangeLogRepository;
import com.qd.mashu.repository.RiderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderService.class);

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private LevelChangeLogRepository levelChangeLogRepository;

    @Autowired
    private TrainingStationService trainingStationService;

    @Transactional
    public RiderResponse create(RiderRequest request) {
        if (riderRepository.existsByRiderCode(request.getRiderCode())) {
            throw new IllegalArgumentException("骑手编号已存在");
        }

        TrainingLevel level = TrainingLevel.fromCode(request.getCurrentLevel());

        Rider rider = Rider.builder()
                .riderCode(request.getRiderCode())
                .riderName(request.getRiderName())
                .age(request.getAge())
                .currentLevel(level)
                .phone(request.getPhone())
                .email(request.getEmail())
                .lastFitnessTestDate(request.getLastFitnessTestDate())
                .status(1)
                .build();

        rider = riderRepository.save(rider);
        logger.info("Created rider: {}", rider.getRiderCode());

        return RiderResponse.fromEntity(rider);
    }

    @Transactional
    public RiderResponse update(Long id, RiderRequest request) {
        Rider rider = riderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));

        if (!rider.getRiderCode().equals(request.getRiderCode())
                && riderRepository.existsByRiderCode(request.getRiderCode())) {
            throw new IllegalArgumentException("骑手编号已存在");
        }

        rider.setRiderCode(request.getRiderCode());
        rider.setRiderName(request.getRiderName());
        rider.setAge(request.getAge());
        rider.setPhone(request.getPhone());
        rider.setEmail(request.getEmail());
        rider.setLastFitnessTestDate(request.getLastFitnessTestDate());

        rider = riderRepository.save(rider);
        logger.info("Updated rider: {}", rider.getRiderCode());

        return RiderResponse.fromEntity(rider);
    }

    @Transactional
    public void delete(Long id) {
        Rider rider = riderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        rider.setStatus(0);
        riderRepository.save(rider);
        logger.info("Deleted rider: {}", rider.getRiderCode());
    }

    public RiderResponse getById(Long id) {
        Rider rider = riderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        return RiderResponse.fromEntity(rider);
    }

    public RiderResponse getByCode(String code) {
        Rider rider = riderRepository.findByRiderCode(code)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        return RiderResponse.fromEntity(rider);
    }

    public List<RiderResponse> listAll() {
        return riderRepository.findByStatus(1).stream()
                .map(RiderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RiderResponse> listByLevel(Integer level) {
        TrainingLevel trainingLevel = TrainingLevel.fromCode(level);
        return riderRepository.findByCurrentLevelAndStatus(trainingLevel, 1).stream()
                .map(RiderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RiderResponse updateLevel(RiderLevelUpdateRequest request) {
        // 改级原因、操作人必填，少写一样就拦下，提示还没写全
        if (request.getChangeReason() == null || request.getChangeReason().isBlank()
                || request.getOperator() == null || request.getOperator().isBlank()) {
            throw new IllegalArgumentException("改级原因和操作人还没写全，请补充完整后再提交");
        }

        // 改级全程锁住骑手档案行，并发连点的第二单会等第一单提交后再读
        Rider rider = riderRepository.findWithLockById(request.getRiderId())
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));

        TrainingLevel newLevel = TrainingLevel.fromCode(request.getNewLevel());
        TrainingLevel previousLevel = rider.getCurrentLevel();

        if (newLevel.getCode() < previousLevel.getCode()) {
            throw new IllegalArgumentException("骑手等级只能升级，不能降级");
        }

        // 幂等：同一骑手同一新等级只生效一次。网络卡顿连点/重试时骑手已在该等级，
        // 直接返回当前状态，不再记一条变更
        if (newLevel.equals(previousLevel)) {
            logger.info("Duplicate level update ignored: rider[{}] already at level[{}]",
                    rider.getRiderCode(), newLevel.getName());
            return RiderResponse.fromEntity(rider);
        }

        LevelChangeLog changeLog = LevelChangeLog.builder()
                .rider(rider)
                .previousLevel(previousLevel)
                .newLevel(newLevel)
                .changeReason(request.getChangeReason())
                .operator(request.getOperator())
                .build();

        levelChangeLogRepository.save(changeLog);
        logger.info("Created level change log for rider: {}", rider.getRiderCode());

        rider.setCurrentLevel(newLevel);
        rider = riderRepository.save(rider);
        logger.info("Updated rider level: {} -> {}", rider.getRiderCode(), newLevel.getName());

        trainingStationService.revalidateStationsAfterLevelChange(rider);

        return RiderResponse.fromEntity(rider);
    }
}