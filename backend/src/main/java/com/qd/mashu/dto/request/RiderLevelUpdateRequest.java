package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderLevelUpdateRequest {

    private Long riderId;

    private Integer newLevel;

    private String changeReason;

    private String operator;
}