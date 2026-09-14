package com.qd.mashu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 训练位占用统计：占用数 + 空闲数 = 训练位总数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStationSummary {

    private long total;

    private long occupied;

    private long free;
}
