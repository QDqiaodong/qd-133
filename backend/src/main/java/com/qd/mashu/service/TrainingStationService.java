package com.qd.mashu.service;

import com.qd.mashu.dto.request.TrainingStationRequest;
import com.qd.mashu.dto.response.TrainingStationResponse;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.TrainingStation;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
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
public class TrainingStationService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingStationService.class);

    @Autowired
    private TrainingStationRepository stationRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private ObstacleEquipmentRepository equipmentRepository;

    @Autowired
    private ObstacleEquipmentService equipmentService;

    @Transactional
    public TrainingStationResponse create(TrainingStationRequest request) {
        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new IllegalArgumentException("训练位编号已存在");
        }

        Rider rider = null;
        ObstacleEquipment equipment = null;

        if (request.getRiderId() != null) {
            rider = riderRepository.findById(request.getRiderId())
                    .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        }

        if (request.getEquipmentId() != null) {
            equipment = equipmentRepository.findById(request.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        }

        if (rider != null && equipment != null) {
            equipmentService.validateLevelMatch(rider.getRiderName(), rider.getCurrentLevel(), equipment);
        }

        TrainingStation station = TrainingStation.builder()
                .stationCode(request.getStationCode())
                .stationName(request.getStationName())
                .rider(rider)
                .equipment(equipment)
                .status(1)
                .build();

        station = stationRepository.save(station);
        logger.info("Created training station: {}", station.getStationCode());

        return TrainingStationResponse.fromEntity(station);
    }

    @Transactional
    public TrainingStationResponse update(Long id, TrainingStationRequest request) {
        TrainingStation station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("训练位不存在"));

        if (!station.getStationCode().equals(request.getStationCode())
                && stationRepository.existsByStationCode(request.getStationCode())) {
            throw new IllegalArgumentException("训练位编号已存在");
        }

        Rider rider = null;
        ObstacleEquipment equipment = null;

        if (request.getRiderId() != null) {
            rider = riderRepository.findById(request.getRiderId())
                    .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        }

        if (request.getEquipmentId() != null) {
            equipment = equipmentRepository.findById(request.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        }

        if (rider != null && equipment != null) {
            equipmentService.validateLevelMatch(rider.getRiderName(), rider.getCurrentLevel(), equipment);
        }

        station.setStationCode(request.getStationCode());
        station.setStationName(request.getStationName());
        station.setRider(rider);
        station.setEquipment(equipment);

        station = stationRepository.save(station);
        logger.info("Updated training station: {}", station.getStationCode());

        return TrainingStationResponse.fromEntity(station);
    }

    @Transactional
    public void delete(Long id) {
        TrainingStation station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("训练位不存在"));
        station.setStatus(0);
        stationRepository.save(station);
        logger.info("Deleted training station: {}", station.getStationCode());
    }

    public TrainingStationResponse getById(Long id) {
        TrainingStation station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("训练位不存在"));
        return TrainingStationResponse.fromEntity(station);
    }

    public List<TrainingStationResponse> listAll() {
        return stationRepository.findByStatus(1).stream()
                .map(TrainingStationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TrainingStationResponse> listByRider(Long riderId) {
        return stationRepository.findByRiderId(riderId).stream()
                .filter(s -> s.getStatus() == 1)
                .map(TrainingStationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TrainingStationResponse> listByEquipment(Long equipmentId) {
        return stationRepository.findByEquipmentId(equipmentId).stream()
                .filter(s -> s.getStatus() == 1)
                .map(TrainingStationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingStationResponse bindRiderAndEquipment(Long stationId, Long riderId, Long equipmentId) {
        TrainingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("训练位不存在"));

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));

        ObstacleEquipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));

        equipmentService.validateLevelMatch(rider.getRiderName(), rider.getCurrentLevel(), equipment);

        station.setRider(rider);
        station.setEquipment(equipment);

        station = stationRepository.save(station);
        logger.info("Bound rider[{}] and equipment[{}] to station[{}]",
                rider.getRiderCode(), equipment.getEquipmentCode(), station.getStationCode());

        return TrainingStationResponse.fromEntity(station);
    }

    @Transactional
    public void revalidateStationsAfterLevelChange(Rider rider) {
        List<TrainingStation> stations = stationRepository.findByRiderIdAndStatus(rider.getId(), 1).stream()
                .collect(Collectors.toList());

        for (TrainingStation station : stations) {
            if (station.getEquipment() != null) {
                try {
                    equipmentService.validateLevelMatch(rider.getRiderName(), rider.getCurrentLevel(), station.getEquipment());
                    logger.info("Station[{}] validation passed after rider level upgrade", station.getStationCode());
                } catch (Exception e) {
                    logger.warn("Station[{}] validation failed after rider level upgrade: {}",
                            station.getStationCode(), e.getMessage());
                }
            }
        }
    }
}