package com.qd.mashu.controller;

import com.qd.mashu.dto.request.HeightRecheckRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.ObstacleEquipmentResponse;
import com.qd.mashu.service.HeightRecheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 杆高复核：教练提交实测高度和复测人；列表可按是否已复核筛选。
 */
@RestController
@RequestMapping("/api/recheck")
public class HeightRecheckController {

    @Autowired
    private HeightRecheckService recheckService;

    /**
     * 杆高复核列表。
     *
     * @param rechecked 可选：true=仅已复核，false=仅未复核，不传=全部
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ObstacleEquipmentResponse>>> list(
            @RequestParam(required = false) Boolean rechecked) {
        return ResponseEntity.ok(ApiResponse.success(recheckService.listRechecks(rechecked)));
    }

    /** 提交某根杆的杆高复核（实测高度 + 复测人）。 */
    @PostMapping("/{equipmentId}")
    public ResponseEntity<ApiResponse<ObstacleEquipmentResponse>> submit(
            @PathVariable Long equipmentId,
            @RequestBody HeightRecheckRequest request) {
        ObstacleEquipmentResponse response = recheckService.submitRecheck(equipmentId, request);
        return ResponseEntity.ok(ApiResponse.success("复核提交成功", response));
    }
}
