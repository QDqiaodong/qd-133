package com.qd.mashu.service;

import com.qd.mashu.dto.request.ObstacleEquipmentRequest;
import com.qd.mashu.dto.response.ObstacleEquipmentResponse;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.exception.LevelMismatchException;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
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
        equipment.setObstacleHeight(request.getObstacleHeight());
        equipment.setAdaptLevel(TrainingLevel.fromCode(request.getAdaptLevel()));
        equipment.setDescription(request.getDescription());

        equipment = equipmentRepository.save(equipment);
        logger.info("Updated obstacle equipment: {}", equipment.getEquipmentCode());

        levelCacheService.refreshLevelCache();

        return ObstacleEquipmentResponse.fromEntity(equipment);
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