package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课后点评提交请求：选好骑手，写下本节重点和一到五星。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReviewRequest {

    /** 骑手ID */
    private Long riderId;

    /** 本节重点 */
    private String sessionFocus;

    /** 星级，一到五星 */
    private Integer starRating;
}
