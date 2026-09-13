package com.qd.mashu.dto.response;

import com.qd.mashu.entity.TrainingStation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStationResponse {

    private Long id;

    private String stationCode;

    private String stationName;

    private RiderResponse rider;

    private ObstacleEquipmentResponse equipment;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static TrainingStationResponse fromEntity(TrainingStation entity) {
        return TrainingStationResponse.builder()
                .id(entity.getId())
                .stationCode(entity.getStationCode())
                .stationName(entity.getStationName())
                .rider(entity.getRider() != null ? RiderResponse.fromEntity(entity.getRider()) : null)
                .equipment(entity.getEquipment() != null ? ObstacleEquipmentResponse.fromEntity(entity.getEquipment()) : null)
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}