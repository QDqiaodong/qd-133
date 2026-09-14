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

    /**
     * 是否占用：已挂上骑手为占用，未挂人为空闲
     */
    private Boolean occupied;

    private String occupancyStatus;

    private String occupancyStatusName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static TrainingStationResponse fromEntity(TrainingStation entity) {
        boolean occupied = entity.getRider() != null;
        return TrainingStationResponse.builder()
                .id(entity.getId())
                .stationCode(entity.getStationCode())
                .stationName(entity.getStationName())
                .rider(occupied ? RiderResponse.fromEntity(entity.getRider()) : null)
                .equipment(entity.getEquipment() != null ? ObstacleEquipmentResponse.fromEntity(entity.getEquipment()) : null)
                .status(entity.getStatus())
                .occupied(occupied)
                .occupancyStatus(occupied ? "OCCUPIED" : "FREE")
                .occupancyStatusName(occupied ? "占用" : "空闲")
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}