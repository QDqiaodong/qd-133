package com.qd.mashu.service;

import com.qd.mashu.dto.request.RiderLevelUpdateRequest;
import com.qd.mashu.dto.request.RiderRequest;
import com.qd.mashu.dto.response.RiderResponse;
import com.qd.mashu.entity.LevelChangeLog;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.TrainingStation;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.LevelChangeLogRepository;
import com.qd.mashu.repository.RiderRepository;
import com.qd.mashu.repository.TrainingStationRepository;
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
    private TrainingStationRepository trainingStationRepository;

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

    /**
     * 停用骑手档案。
     * 只要该骑手还挂在任一启用中的训练位上，整次停用失败并逐位写明占着哪些位；
     * 必须先从训练位拿下（杆保留在原位），才能停用。
     *
     * 与「往空位上挂这个人」并发时，两边都走骑手行悲观写锁，锁内复查占用与档案状态：
     * 谁先拿到锁谁成，后到的一单看到对方已提交的结果后自行失败——
     * 要么停用成功且位上没有这个人，要么挂上成功且档案仍在用，不会出现人已停用还占着位。
     */
    @Transactional
    public void delete(Long id) {
        Rider rider = riderRepository.findWithLockById(id)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));

        List<TrainingStation> occupiedStations =
                trainingStationRepository.findByRiderIdAndStatus(id, 1);
        if (!occupiedStations.isEmpty()) {
            String stationNames = occupiedStations.stream()
                    .map(s -> s.getStationName() + "（" + s.getStationCode() + "）")
                    .collect(Collectors.joining("、"));
            throw new IllegalArgumentException(
                    "骑手[" + rider.getRiderName() + "]还占着训练位：" + stationNames
                            + "，请先把该骑手从这些训练位拿下（杆可以留在原位）再停用");
        }

        rider.setStatus(0);
        riderRepository.save(rider);
        logger.info("Deactivated rider: {}", rider.getRiderCode());
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

    /**
     * 列出全部骑手，含已停用：训练位绑定下拉要用，停用的人在下拉里看得到、选得到，
     * 但提交时由训练位一侧拦回（停用档案不能再绑到任何训练位）。
     */
    public List<RiderResponse> listAllIncludeInactive() {
        return riderRepository.findAll().stream()
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