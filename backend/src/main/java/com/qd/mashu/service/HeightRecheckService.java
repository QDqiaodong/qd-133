package com.qd.mashu.service;

import com.qd.mashu.dto.request.HeightRecheckRequest;
import com.qd.mashu.dto.response.ObstacleEquipmentResponse;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import com.qd.mashu.repository.TrainingStationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 杆高复核服务。
 *
 * <p>复核结论与“能否绑定”均在服务端按 {@link HeightRecheckRules} 统一计算并持久化：
 * 未复核或高度不符的杆一律不能绑上训练位；已绑定的杆复核出高度不符会被立即拆下。
 */
@Service
public class HeightRecheckService {

    private static final Logger logger = LoggerFactory.getLogger(HeightRecheckService.class);

    /** 器材状态：在修中。 */
    private static final int STATUS_IN_REPAIR = 2;

    @Autowired
    private ObstacleEquipmentRepository equipmentRepository;

    @Autowired
    private TrainingStationRepository stationRepository;

    @Autowired
    private ObstacleEquipmentService equipmentService;

    /**
     * 提交杆高复核：教练填写实测高度和复测人。
     *
     * @return 带最新复核结论与绑定资格的器材信息
     */
    @Transactional
    public ObstacleEquipmentResponse submitRecheck(Long equipmentId, HeightRecheckRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("复核信息不能为空");
        }
        if (request.getMeasuredHeight() == null) {
            throw new IllegalArgumentException("请填写实测高度");
        }
        if (request.getMeasuredHeight() <= 0 || request.getMeasuredHeight() > 300) {
            throw new IllegalArgumentException("实测高度需在 0~300cm 之间");
        }
        if (request.getReviewer() == null || request.getReviewer().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写复测人");
        }

        ObstacleEquipment equipment = equipmentRepository.findWithLockById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        if (equipment.getStatus() != null && equipment.getStatus() == STATUS_IN_REPAIR) {
            throw new IllegalArgumentException("杆[" + equipment.getEquipmentName() + "]正在送修，暂不能提交杆高复核");
        }

        double nominal = equipment.getObstacleHeight();
        double measured = request.getMeasuredHeight();
        String result = HeightRecheckRules.evaluate(nominal, measured);
        double diff = HeightRecheckRules.diff(nominal, measured);

        equipment.setMeasuredHeight(measured);
        equipment.setRecheckReviewer(request.getReviewer().trim());
        equipment.setRecheckResult(result);
        equipment.setRecheckHeightDiff(diff);
        equipment.setRecheckTime(LocalDateTime.now());
        equipment = equipmentRepository.save(equipment);

        logger.info("Height recheck submitted: equipment[{}] nominal={}cm measured={}cm diff={}cm result={} reviewer={}",
                equipment.getEquipmentCode(), nominal, measured, diff, result, equipment.getRecheckReviewer());

        // 高度不符的杆必须拦住绑定：若之前已绑在训练位上，立即拆下来。
        if (HeightRecheckRules.RESULT_MISMATCH.equals(result)) {
            int unbound = equipmentService.detachFromStations(equipment);
            if (unbound > 0) {
                logger.warn("Equipment[{}] failed height recheck, detached from {} station(s)",
                        equipment.getEquipmentCode(), unbound);
            }
        }

        return ObstacleEquipmentResponse.fromEntity(equipment);
    }

    /**
     * 绑定前校验：未做杆高复核或复核结论为高度不符的杆，一律拦住不能绑上训练位。
     */
    public void validateBindable(ObstacleEquipment equipment) {
        boolean rechecked = HeightRecheckRules.isRechecked(
                equipment.getMeasuredHeight(), equipment.getRecheckReviewer());
        if (!rechecked) {
            throw new IllegalArgumentException(
                    "杆[" + equipment.getEquipmentName() + "]未做杆高复核，不能绑上训练位");
        }
        if (!HeightRecheckRules.canBind(rechecked, equipment.getRecheckResult())) {
            throw new IllegalArgumentException(
                    "杆[" + equipment.getEquipmentName() + "]复核结论为高度不符（标称 "
                            + equipment.getObstacleHeight() + "cm / 实测 "
                            + equipment.getMeasuredHeight() + "cm，相差超过约定 "
                            + HeightRecheckRules.RECHECK_TOLERANCE_CM + "cm），已拦住绑定");
        }
    }

    /**
     * 列出器材及其复核状态。
     *
     * @param rechecked null=全部，true=仅已复核，false=仅未复核
     */
    public List<ObstacleEquipmentResponse> listRechecks(Boolean rechecked) {
        return equipmentRepository.findByStatus(1).stream()
                .filter(e -> {
                    if (rechecked == null) {
                        return true;
                    }
                    boolean done = HeightRecheckRules.isRechecked(e.getMeasuredHeight(), e.getRecheckReviewer());
                    return done == rechecked;
                })
                .map(ObstacleEquipmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
