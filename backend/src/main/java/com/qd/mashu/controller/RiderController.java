package com.qd.mashu.controller;

import com.qd.mashu.dto.request.RiderLevelUpdateRequest;
import com.qd.mashu.dto.request.RiderRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.LevelChangeLogResponse;
import com.qd.mashu.dto.response.RiderResponse;
import com.qd.mashu.service.LevelChangeLogService;
import com.qd.mashu.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rider")
public class RiderController {

    @Autowired
    private RiderService riderService;

    @Autowired
    private LevelChangeLogService levelChangeLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<RiderResponse>> create(@RequestBody RiderRequest request) {
        RiderResponse response = riderService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RiderResponse>> update(@PathVariable Long id, @RequestBody RiderRequest request) {
        RiderResponse response = riderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        riderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RiderResponse>> getById(@PathVariable Long id) {
        RiderResponse response = riderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<RiderResponse>> getByCode(@PathVariable String code) {
        RiderResponse response = riderService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RiderResponse>>> listAll(
            @RequestParam(value = "includeInactive", required = false, defaultValue = "false") Boolean includeInactive) {
        List<RiderResponse> response = Boolean.TRUE.equals(includeInactive)
                ? riderService.listAllIncludeInactive()
                : riderService.listAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<RiderResponse>>> listByLevel(@PathVariable Integer level) {
        List<RiderResponse> response = riderService.listByLevel(level);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/level/update")
    public ResponseEntity<ApiResponse<RiderResponse>> updateLevel(@RequestBody RiderLevelUpdateRequest request) {
        RiderResponse response = riderService.updateLevel(request);
        return ResponseEntity.ok(ApiResponse.success("等级更新成功", response));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<LevelChangeLogResponse>>> getLevelLogs(@PathVariable Long id) {
        List<LevelChangeLogResponse> response = levelChangeLogService.getLogsByRider(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}