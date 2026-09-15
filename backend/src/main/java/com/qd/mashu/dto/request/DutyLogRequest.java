package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 值班记录提交请求：日期、当班教练、接班教练、今晚留意事项都必须填写。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyLogRequest {

    /** 值班日期，格式 YYYY-MM-DD */
    private LocalDate dutyDate;

    /** 当班教练 */
    private String onDutyCoach;

    /** 接班教练 */
    private String incomingCoach;

    /** 今晚要留意的事 */
    private String tonightNotes;
}
