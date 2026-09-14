package com.qd.mashu.service;

import com.qd.mashu.dto.request.ObstacleEquipmentRequest;
import com.qd.mashu.dto.response.ObstacleEquipmentResponse;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.entity.TrainingStation;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.exception.LevelMismatchException;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import com.qd.mashu.repository.TrainingStationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObstacleEquipmentService {

    private static final Logger logger = LoggerFactory.getLogger(ObstacleEquipmentService.class);

    @Autowired
    private ObstacleEquipmentRepository equipmentRepository;

    @Autowired
    private TrainingStationRepository stationRepository;

    @Autowired
    private LevelCacheService levelCacheService;

    @Transactional
    public ObstacleEquipmentResponse create(ObstacleEquipmentRequest request) {
        if (equipmentRepository.existsByEquipmentCode(request.getEquipmentCode())) {
            throw new IllegalArgumentException("设备编号已存在");
        }

        TrainingLevel adaptLevel = TrainingLevel.fromCode(request.getAdaptLevel());

        ObstacleEquipment equipment = ObstacleEquipment.builder()
                .equipmentCode(request.getEquipmentCode())
                .equipmentName(request.getEquipmentName())
                .obstacleHeight(request.getObstacleHeight())
                .adaptLevel(adaptLevel)
                .description(request.getDescription())
                .status(1)
                .build();

        equipment = equipmentRepository.save(equipment);
        logger.info("Created obstacle equipment: {}", equipment.getEquipmentCode());

        levelCacheService.refreshLevelCache();

        return ObstacleEquipmentResponse.fromEntity(equipment);
    }

    @Transactional
    public ObstacleEquipmentResponse update(Long id, ObstacleEquipmentRequest request) {
        ObstacleEquipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));

        if (!equipment.getEquipmentCode().equals(request.getEquipmentCode())
                && equipmentRepository.existsByEquipmentCode(request.getEquipmentCode())) {
            throw new IllegalArgumentException("设备编号已存在");
        }

        equipment.setEquipmentCode(request.getEquipmentCode());
        equipment.setEquipmentName(request.getEquipmentName());

        // 档案标称高度一旦修改，原杆高复核结论即失效，必须重新复核；
        // 已绑在训练位上的也要先拆下来，避免“标称对不上实测”的杆继续占着训练位。
        boolean nominalHeightChanged = request.getObstacleHeight() != null
                && Double.compare(request.getObstacleHeight(), equipment.getObstacleHeight()) != 0;

        equipment.setObstacleHeight(request.getObstacleHeight());
        equipment.setAdaptLevel(TrainingLevel.fromCode(request.getAdaptLevel()));
        equipment.setDescription(request.getDescription());

        int unbound = 0;
        if (nominalHeightChanged) {
            clearRecheck(equipment);
            unbound = detachFromStations(equipment);
            logger.info("Nominal height changed for equipment[{}], recheck reset and {} station(s) detached",
                    equipment.getEquipmentCode(), unbound);
        }

        equipment = equipmentRepository.save(equipment);
        logger.info("Updated obstacle equipment: {}{}",
                equipment.getEquipmentCode(),
                nominalHeightChanged ? "，标称高度已变更，原复核结论失效" : "");

        levelCacheService.refreshLevelCache();

        return ObstacleEquipmentResponse.fromEntity(equipment);
    }

    /**
     * 清除某根杆的杆高复核结论。
     */
    public void clearRecheck(ObstacleEquipment equipment) {
        equipment.setMeasuredHeight(null);
        equipment.setRecheckReviewer(null);
        equipment.setRecheckResult(null);
        equipment.setRecheckHeightDiff(null);
        equipment.setRecheckTime(null);
    }

    /**
     * 把某根杆从所有在用训练位上拆下来，返回拆下的训练位数量。
     */
    public int detachFromStations(ObstacleEquipment equipment) {
        List<TrainingStation> boundStations = stationRepository
                .findByEquipmentId(equipment.getId()).stream()
                .filter(s -> s.getStatus() != null && s.getStatus() == 1)
                .collect(Collectors.toList());
        for (TrainingStation station : boundStations) {
            station.setEquipment(null);
            stationRepository.save(station);
            logger.warn("Detached equipment[{}] from station[{}] because its height recheck is no longer valid",
                    equipment.getEquipmentCode(), station.getStationCode());
        }
        return boundStations.size();
    }

    @Transactional
    public void delete(Long id) {
        ObstacleEquipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        equipment.setStatus(0);
        equipmentRepository.save(equipment);
        logger.info("Deleted obstacle equipment: {}", equipment.getEquipmentCode());

        levelCacheService.refreshLevelCache();
    }

    public ObstacleEquipmentResponse getById(Long id) {
        ObstacleEquipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        return ObstacleEquipmentResponse.fromEntity(equipment);
    }

    public ObstacleEquipmentResponse getByCode(String code) {
        ObstacleEquipment equipment = equipmentRepository.findByEquipmentCode(code)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        return ObstacleEquipmentResponse.fromEntity(equipment);
    }

    public List<ObstacleEquipmentResponse> listAll() {
        return equipmentRepository.findByStatus(1).stream()
                .map(ObstacleEquipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ObstacleEquipmentResponse> listByLevel(Integer level) {
        TrainingLevel trainingLevel = TrainingLevel.fromCode(level);
        return equipmentRepository.findByAdaptLevelAndStatus(trainingLevel, 1).stream()
                .map(ObstacleEquipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public void validateLevelMatch(String riderName, TrainingLevel riderLevel, ObstacleEquipment equipment) {
        if (!equipment.getAdaptLevel().isCompatibleWith(riderLevel)) {
            throw new LevelMismatchException(riderName, riderLevel.getName(),
                    equipment.getEquipmentName(), equipment.getAdaptLevel().getName());
        }
        logger.debug("Level match validated: rider[{}] level[{}] can use equipment[{}] level[{}]",
                riderName, riderLevel.getName(), equipment.getEquipmentName(), equipment.getAdaptLevel().getName());
    }
}