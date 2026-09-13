package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleEquipmentRequest {

    private String equipmentCode;

    private String equipmentName;

    private Double obstacleHeight;

    private Integer adaptLevel;

    private String description;
}