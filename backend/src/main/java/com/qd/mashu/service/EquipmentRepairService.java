package com.qd.mashu.service;

import com.qd.mashu.dto.request.EquipmentRepairReturnRequest;
import com.qd.mashu.dto.request.EquipmentRepairSendRequest;
import com.qd.mashu.dto.response.EquipmentRepairOrderResponse;
import com.qd.mashu.entity.EquipmentRepairOrder;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.repository.EquipmentRepairOrderRepository;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 障碍杆送修服务。
 *
 * <p>送修不是软删除：开单时必须填写故障说明和经办教练，并且在同一事务中先把杆从训练位拆下；
 * 修好归还是第二个独立步骤，必须填写修复结论后，器材才能恢复成在用。
 */
@Service
public class EquipmentRepairService {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentRepairService.class);

    /** 器材在用。 */
    public static final int EQUIPMENT_STATUS_IN_USE = 1;

    /** 器材在修中。 */
    public static final int EQUIPMENT_STATUS_IN_REPAIR = 2;

    /** 器材已删除/停用。 */
    public static final int EQUIPMENT_STATUS_DELETED = 0;

    public static final String ORDER_STATUS_IN_REPAIR = "IN_REPAIR";
    public static final String ORDER_STATUS_RETURNED = "RETURNED";

    @Autowired
    private EquipmentRepairOrderRepository repairOrderRepository;

    @Autowired
    private ObstacleEquipmentRepository equipmentRepository;

    @Autowired
    private ObstacleEquipmentService equipmentService;

    @Autowired
    private LevelCacheService levelCacheService;

    /**
     * 开单送修。
     *
     * <p>器材行先加悲观写锁，随后复查在修名单和训练位绑定：
     * 与“再绑到训练位”并发时，绑定也要拿同一把行锁，后提交的一单在锁内会看到在修状态并失败。
     */
    @Transactional
    public EquipmentRepairOrderResponse sendForRepair(Long equipmentId, EquipmentRepairSendRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("送修信息不能为空");
        }
        if (request.getFaultDescription() == null || request.getFaultDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写故障说明");
        }
        if (request.getHandlerCoach() == null || request.getHandlerCoach().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写经办教练");
        }

        ObstacleEquipment equipment = equipmentRepository.findWithLockById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));

        if (equipment.getStatus() != null && equipment.getStatus() == EQUIPMENT_STATUS_IN_REPAIR) {
            throw new IllegalArgumentException("杆[" + equipment.getEquipmentName() + "]已在修中，不能重复开单");
        }
        if (equipment.getStatus() == null || equipment.getStatus() == EQUIPMENT_STATUS_DELETED) {
            throw new IllegalArgumentException("杆[" + equipment.getEquipmentName() + "]已删除，不能送修");
        }

        int detachedCount = equipmentService.detachFromStations(equipment);

        LocalDateTime now = LocalDateTime.now();
        EquipmentRepairOrder order = EquipmentRepairOrder.builder()
                .equipment(equipment)
                .faultDescription(request.getFaultDescription().trim())
                .handlerCoach(request.getHandlerCoach().trim())
                .status(ORDER_STATUS_IN_REPAIR)
                .sentTime(now)
                .createTime(now)
                .build();
        order = repairOrderRepository.save(order);

        equipment.setStatus(EQUIPMENT_STATUS_IN_REPAIR);
        equipmentRepository.save(equipment);

        levelCacheService.refreshLevelCache();
        logger.info("Equipment[{}] sent for repair, orderId={}, detached from {} station(s)",
                equipment.getEquipmentCode(), order.getId(), detachedCount);

        return EquipmentRepairOrderResponse.fromEntity(order);
    }

    /**
     * 修好归还：必须填写修复结论；没有结论不能把器材恢复成在用。
     */
    @Transactional
    public EquipmentRepairOrderResponse returnFromRepair(Long orderId, EquipmentRepairReturnRequest request) {
        if (request == null || request.getRepairConclusion() == null || request.getRepairConclusion().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写修复结论");
        }

        EquipmentRepairOrder order = repairOrderRepository.findWithLockById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("维修单不存在"));
        if (!ORDER_STATUS_IN_REPAIR.equals(order.getStatus())) {
            throw new IllegalArgumentException("该维修单已归还，不能重复归还");
        }

        ObstacleEquipment equipment = equipmentRepository.findWithLockById(order.getEquipment().getId())
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        if (equipment.getStatus() == null || equipment.getStatus() != EQUIPMENT_STATUS_IN_REPAIR) {
            throw new IllegalArgumentException("杆[" + equipment.getEquipmentName() + "]当前不在修中，不能归还");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setRepairConclusion(request.getRepairConclusion().trim());
        order.setStatus(ORDER_STATUS_RETURNED);
        order.setReturnedTime(now);
        order = repairOrderRepository.save(order);

        equipment.setStatus(EQUIPMENT_STATUS_IN_USE);
        equipmentRepository.save(equipment);

        levelCacheService.refreshLevelCache();
        logger.info("Equipment[{}] returned from repair, orderId={}", equipment.getEquipmentCode(), order.getId());

        return EquipmentRepairOrderResponse.fromEntity(order);
    }

    /**
     * 绑定前复查器材状态：在修或已删除的杆都不能挂回训练位。
     * 调用方必须通过 findWithLockById 取器材，保证与送修/归还并发时读到的是锁内最新状态。
     */
    public void validateCanBind(ObstacleEquipment equipment) {
        Integer status = equipment.getStatus();
        if (status == null || status == EQUIPMENT_STATUS_DELETED) {
            throw new IllegalArgumentException("杆[" + equipment.getEquipmentName() + "]已删除，不能绑上训练位");
        }
        if (status == EQUIPMENT_STATUS_IN_REPAIR) {
            throw new IllegalArgumentException("杆[" + equipment.getEquipmentName() + "]正在送修，不能绑上训练位");
        }
    }

    @Transactional(readOnly = true)
    public List<EquipmentRepairOrderResponse> listOrders(String status) {
        List<EquipmentRepairOrder> orders;
        if (status == null || status.trim().isEmpty()) {
            orders = repairOrderRepository.findAllByOrderBySentTimeDesc();
        } else if (ORDER_STATUS_IN_REPAIR.equals(status) || ORDER_STATUS_RETURNED.equals(status)) {
            orders = repairOrderRepository.findByStatusOrderBySentTimeDesc(status);
        } else {
            throw new IllegalArgumentException("维修单状态不正确");
        }
        return orders.stream()
                .map(EquipmentRepairOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
