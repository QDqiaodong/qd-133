package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 障碍杆送修开单请求：故障说明和经办教练都必须填写。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRepairSendRequest {

    /** 故障说明 */
    private String faultDescription;

    /** 经办教练 */
    private String handlerCoach;
}
