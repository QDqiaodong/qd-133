package com.qd.mashu.dto.response;

import com.qd.mashu.entity.SessionReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReviewResponse {

    private Long id;

    private RiderResponse rider;

    private String sessionFocus;

    private Integer starRating;

    private LocalDateTime createTime;

    public static SessionReviewResponse fromEntity(SessionReview entity) {
        return SessionReviewResponse.builder()
                .id(entity.getId())
                .rider(RiderResponse.fromEntity(entity.getRider()))
                .sessionFocus(entity.getSessionFocus())
                .starRating(entity.getStarRating())
                .createTime(entity.getCreateTime())
                .build();
    }
}
