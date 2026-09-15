package com.qd.mashu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课后点评星级汇总：按一到五星分桶统计条数。
 * total 为各星级条数之和，与点评明细条数同源同表，必然一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReviewSummary {

    /** 点评总条数（= 各星级条数之和 = 点评明细条数） */
    private Long total;

    /** 平均星级，保留一位小数；无点评时为 0 */
    private Double averageStars;

    /** 一到五星各星级条数，固定 5 桶，没评过的星级条数为 0 */
    private List<StarCount> starCounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StarCount {

        /** 星级（1~5） */
        private Integer stars;

        /** 该星级的点评条数 */
        private Long count;
    }
}
