package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 障碍杆修好归还请求：修复结论必须填写。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRepairReturnRequest {

    /** 修复结论 */
    private String repairConclusion;
}
