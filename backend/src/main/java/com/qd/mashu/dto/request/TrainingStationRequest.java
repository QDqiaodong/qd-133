package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStationRequest {

    private String stationCode;

    private String stationName;

    private Long riderId;

    private Long equipmentId;
}