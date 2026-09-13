package com.qd.mashu.dto.response;

import com.qd.mashu.entity.ObstacleEquipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleEquipmentResponse {

    private Long id;

    private String equipmentCode;

    private String equipmentName;

    private Double obstacleHeight;

    private Integer adaptLevel;

    private String adaptLevelName;

    private String adaptLevelDesc;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static ObstacleEquipmentResponse fromEntity(ObstacleEquipment entity) {
        return ObstacleEquipmentResponse.builder()
                .id(entity.getId())
                .equipmentCode(entity.getEquipmentCode())
                .equipmentName(entity.getEquipmentName())
                .obstacleHeight(entity.getObstacleHeight())
                .adaptLevel(entity.getAdaptLevel().getCode())
                .adaptLevelName(entity.getAdaptLevel().getName())
                .adaptLevelDesc(entity.getAdaptLevel().getDescription())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}