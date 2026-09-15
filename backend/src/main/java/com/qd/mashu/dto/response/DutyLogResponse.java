package com.qd.mashu.dto.response;

import com.qd.mashu.entity.DutyLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogResponse {

    private Long id;

    private LocalDate dutyDate;

    private String onDutyCoach;

    private String incomingCoach;

    private String tonightNotes;

    private LocalDateTime createTime;

    public static DutyLogResponse fromEntity(DutyLog entity) {
        return DutyLogResponse.builder()
                .id(entity.getId())
                .dutyDate(entity.getDutyDate())
                .onDutyCoach(entity.getOnDutyCoach())
                .incomingCoach(entity.getIncomingCoach())
                .tonightNotes(entity.getTonightNotes())
                .createTime(entity.getCreateTime())
                .build();
    }
}
