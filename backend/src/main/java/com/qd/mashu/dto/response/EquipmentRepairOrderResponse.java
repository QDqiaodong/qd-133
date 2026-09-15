package com.qd.mashu.dto.response;

import com.qd.mashu.entity.EquipmentRepairOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRepairOrderResponse {

    private Long id;

    private Long equipmentId;

    private String equipmentCode;

    private String equipmentName;

    private String faultDescription;

    private String handlerCoach;

    /** IN_REPAIR / RETURNED */
    private String status;

    /** 在修中 / 已归还 */
    private String statusName;

    private String repairConclusion;

    private LocalDateTime sentTime;

    private LocalDateTime returnedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static EquipmentRepairOrderResponse fromEntity(EquipmentRepairOrder entity) {
        String status = entity.getStatus();
        return EquipmentRepairOrderResponse.builder()
                .id(entity.getId())
                .equipmentId(entity.getEquipment().getId())
                .equipmentCode(entity.getEquipment().getEquipmentCode())
                .equipmentName(entity.getEquipment().getEquipmentName())
                .faultDescription(entity.getFaultDescription())
                .handlerCoach(entity.getHandlerCoach())
                .status(status)
                .statusName("IN_REPAIR".equals(status) ? "在修中" : "已归还")
                .repairConclusion(entity.getRepairConclusion())
                .sentTime(entity.getSentTime())
                .returnedTime(entity.getReturnedTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
