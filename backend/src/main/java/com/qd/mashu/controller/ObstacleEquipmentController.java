package com.qd.mashu.controller;

import com.qd.mashu.dto.request.ObstacleEquipmentRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.ObstacleEquipmentResponse;
import com.qd.mashu.service.ObstacleEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class ObstacleEquipmentController {

    @Autowired
    private ObstacleEquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ObstacleEquipmentResponse>> create(@RequestBody ObstacleEquipmentRequest request) {
        ObstacleEquipmentResponse response = equipmentService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ObstacleEquipmentResponse>> update(@PathVariable Long id, @RequestBody ObstacleEquipmentRequest request) {
        ObstacleEquipmentResponse response = equipmentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ObstacleEquipmentResponse>> getById(@PathVariable Long id) {
        ObstacleEquipmentResponse response = equipmentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ObstacleEquipmentResponse>> getByCode(@PathVariable String code) {
        ObstacleEquipmentResponse response = equipmentService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ObstacleEquipmentResponse>>> listAll() {
        List<ObstacleEquipmentResponse> response = equipmentService.listAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<ObstacleEquipmentResponse>>> listByLevel(@PathVariable Integer level) {
        List<ObstacleEquipmentResponse> response = equipmentService.listByLevel(level);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}