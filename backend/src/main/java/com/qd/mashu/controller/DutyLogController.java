package com.qd.mashu.controller;

import com.qd.mashu.dto.request.DutyLogRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.DutyLogResponse;
import com.qd.mashu.service.DutyLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 值班记录：每天一条，记录当班教练、接班教练和今晚要留意的事项；
 * 历史记录可按日期翻查。
 */
@RestController
@RequestMapping("/api/duty-log")
public class DutyLogController {

    @Autowired
    private DutyLogService dutyLogService;

    /** 提交当天值班记录，四项内容没填全由后端拦住。 */
    @PostMapping
    public ResponseEntity<ApiResponse<DutyLogResponse>> create(@RequestBody DutyLogRequest request) {
        DutyLogResponse response = dutyLogService.create(request);
        return ResponseEntity.ok(ApiResponse.success("值班记录提交成功", response));
    }

    /** 查询记录：不传日期查全部，传 dutyDate=YYYY-MM-DD 查指定日期。 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DutyLogResponse>>> list(
            @RequestParam(name = "dutyDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dutyDate) {
        return ResponseEntity.ok(ApiResponse.success(dutyLogService.list(dutyDate)));
    }
}
