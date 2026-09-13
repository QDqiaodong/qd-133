package com.qd.mashu.dto.response;

import com.qd.mashu.entity.LevelChangeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelChangeLogResponse {

    private Long id;

    private RiderResponse rider;

    private Integer previousLevel;

    private String previousLevelName;

    private Integer newLevel;

    private String newLevelName;

    private String changeReason;

    private String operator;

    private LocalDateTime createTime;

    public static LevelChangeLogResponse fromEntity(LevelChangeLog entity) {
        return LevelChangeLogResponse.builder()
                .id(entity.getId())
                .rider(RiderResponse.fromEntity(entity.getRider()))
                .previousLevel(entity.getPreviousLevel().getCode())
                .previousLevelName(entity.getPreviousLevel().getName())
                .newLevel(entity.getNewLevel().getCode())
                .newLevelName(entity.getNewLevel().getName())
                .changeReason(entity.getChangeReason())
                .operator(entity.getOperator())
                .createTime(entity.getCreateTime())
                .build();
    }
}