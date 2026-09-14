package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 杆高复核提交请求：教练填写场上实测高度和复测人。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeightRecheckRequest {

    /** 场上实测高度（cm） */
    private Double measuredHeight;

    /** 复测人 */
    private String reviewer;
}
