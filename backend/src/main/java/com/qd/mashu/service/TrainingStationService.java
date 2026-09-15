package com.qd.mashu.service;

import com.qd.mashu.dto.request.TrainingStationRequest;
import com.qd.mashu.dto.response.TrainingStationResponse;
import com.qd.mashu.dto.response.TrainingStationSummary;
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

    @Autowired
    private HeightRecheckService heightRecheckService;

    @Transactional
    public TrainingStationResponse create(TrainingStationRequest request) {
        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new IllegalArgumentException("训练位编号已存在");
        }

        Rider rider = null;
        ObstacleEquipment equipment = null;

        if (request.getRiderId() != null) {
            // 锁住骑手档案行并在锁内复查状态：停用的骑手不能再绑到任何训练位
            rider = loadActiveRiderWithLock(request.getRiderId());
        }

        if (request.getEquipmentId() != null) {
            equipment = equipmentRepository.findById(request.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
            // 未复核 / 高度不符的杆不能绑上训练位
            heightRecheckService.validateBindable(equipment);
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
            // 锁住骑手档案行并在锁内复查状态：停用的骑手不能再绑到任何训练位
            rider = loadActiveRiderWithLock(request.getRiderId());
        }

        if (request.getEquipmentId() != null) {
            equipment = equipmentRepository.findById(request.getEquipmentId())
                    .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
            // 未复核 / 高度不符的杆不能绑上训练位
            heightRecheckService.validateBindable(equipment);
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
        return toResponse(station);
    }

    public List<TrainingStationResponse> listAll() {
        return stationRepository.findByStatus(1).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 训练位占用统计：挂上「在用」骑手的计占用，没挂人（含挂着已停用骑手的历史脏数据）
     * 的计空闲，占用 + 空闲 = 训练位总数。停用的人不再出现在占用名单里。
     */
    public TrainingStationSummary getSummary() {
        List<TrainingStation> stations = stationRepository.findByStatus(1);
        long total = stations.size();
        long occupied = stations.stream().filter(this::isOccupiedByActiveRider).count();
        return TrainingStationSummary.builder()
                .total(total)
                .occupied(occupied)
                .free(total - occupied)
                .build();
    }

    /**
     * 从训练位拿下骑手：骑手离场后该位改标空闲（杆仍留在训练位上）。
     */
    @Transactional
    public TrainingStationResponse unbindRider(Long stationId) {
        TrainingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("训练位不存在"));

        if (station.getRider() == null) {
            throw new IllegalArgumentException("该训练位当前空闲，未挂骑手");
        }

        String riderCode = station.getRider().getRiderCode();
        station.setRider(null);
        station = stationRepository.save(station);
        logger.info("Unbound rider[{}] from station[{}], station is now FREE", riderCode, station.getStationCode());

        return toResponse(station);
    }

    public List<TrainingStationResponse> listByRider(Long riderId) {
        return stationRepository.findByRiderId(riderId).stream()
                .filter(s -> s.getStatus() == 1)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TrainingStationResponse> listByEquipment(Long equipmentId) {
        return stationRepository.findByEquipmentId(equipmentId).stream()
                .filter(s -> s.getStatus() == 1)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingStationResponse bindRiderAndEquipment(Long stationId, Long riderId, Long equipmentId) {
        TrainingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("训练位不存在"));

        // 锁住骑手档案行并在锁内复查状态：停用的骑手不能再绑到任何训练位。
        // 与另一头的「停用档案」并发时，谁先拿到骑手行锁谁成，后到的一单必失败。
        Rider rider = loadActiveRiderWithLock(riderId);

        ObstacleEquipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));

        // 未复核 / 高度不符的杆不能绑上训练位
        heightRecheckService.validateBindable(equipment);

        equipmentService.validateLevelMatch(rider.getRiderName(), rider.getCurrentLevel(), equipment);

        station.setRider(rider);
        station.setEquipment(equipment);

        station = stationRepository.save(station);
        logger.info("Bound rider[{}] and equipment[{}] to station[{}]",
                rider.getRiderCode(), equipment.getEquipmentCode(), station.getStationCode());

        return toResponse(station);
    }

    /**
     * 骑手改级后复核其在位训练位：杆的适配等级若已高于骑手新等级，
     * 该骑手-杆组合在训练位列表中标为「等级不符」（由 TrainingStationResponse 按当前等级实时推导）。
     */
    @Transactional
    public void revalidateStationsAfterLevelChange(Rider rider) {
        List<TrainingStation> stations = stationRepository.findByRiderIdAndStatus(rider.getId(), 1);

        for (TrainingStation station : stations) {
            if (station.getEquipment() != null) {
                try {
                    equipmentService.validateLevelMatch(rider.getRiderName(), rider.getCurrentLevel(), station.getEquipment());
                    logger.info("Station[{}] validation passed after rider level change", station.getStationCode());
                } catch (Exception e) {
                    logger.warn("Station[{}] flagged as level mismatch after rider level change: {}",
                            station.getStationCode(), e.getMessage());
                }
            }
        }
    }

    /**
     * 取骑手档案行的悲观写锁并在锁内复查状态：
     * 停用（status=0）的骑手不能再绑到任何训练位，下拉里选得到，提交在此拦回。
     * 停用操作持同一把行锁，两边并发时后拿锁的一单看到对方已提交的结果，必失败。
     */
    private Rider loadActiveRiderWithLock(Long riderId) {
        Rider rider = riderRepository.findWithLockById(riderId)
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        if (rider.getStatus() == null || rider.getStatus() != 1) {
            throw new IllegalArgumentException(
                    "骑手[" + rider.getRiderName() + "]的档案已停用，不能再绑到训练位");
        }
        return rider;
    }

    private boolean isOccupiedByActiveRider(TrainingStation station) {
        return station.getRider() != null
                && station.getRider().getStatus() != null
                && station.getRider().getStatus() == 1;
    }

    /**
     * 训练位出参：挂着已停用骑手的位按空闲投影（杆照旧保留在位上）。
     * 正常流程下不会产生这种组合（停用前必须先拿下人），这里只兜底历史/异常数据，
     * 保证关掉名单再打开时停用的人不出现在占用名单和占用统计里。
     */
    private TrainingStationResponse toResponse(TrainingStation station) {
        if (isOccupiedByActiveRider(station)) {
            return TrainingStationResponse.fromEntity(station);
        }

        if (station.getRider() != null) {
            // 挂着已停用骑手的位按空闲投影（杆照旧保留），只影响出参、不动库中实体
            logger.warn("Station[{}] still references inactive rider[{}], projecting as FREE",
                    station.getStationCode(), station.getRider().getRiderCode());
            return TrainingStationResponse.fromEntity(station, null);
        }
        return TrainingStationResponse.fromEntity(station);
    }
}