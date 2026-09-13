package com.qd.mashu.controller;

import com.qd.mashu.dto.request.TrainingStationRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.TrainingStationResponse;
import com.qd.mashu.service.TrainingStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/station")
public class TrainingStationController {

    @Autowired
    private TrainingStationService stationService;

    @PostMapping
    public ResponseEntity<ApiResponse<TrainingStationResponse>> create(@RequestBody TrainingStationRequest request) {
        TrainingStationResponse response = stationService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainingStationResponse>> update(@PathVariable Long id, @RequestBody TrainingStationRequest request) {
        TrainingStationResponse response = stationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        stationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainingStationResponse>> getById(@PathVariable Long id) {
        TrainingStationResponse response = stationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainingStationResponse>>> listAll() {
        List<TrainingStationResponse> response = stationService.listAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponse<List<TrainingStationResponse>>> listByRider(@PathVariable Long riderId) {
        List<TrainingStationResponse> response = stationService.listByRider(riderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<ApiResponse<List<TrainingStationResponse>>> listByEquipment(@PathVariable Long equipmentId) {
        List<TrainingStationResponse> response = stationService.listByEquipment(equipmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{stationId}/bind")
    public ResponseEntity<ApiResponse<TrainingStationResponse>> bindRiderAndEquipment(
            @PathVariable Long stationId,
            @RequestParam Long riderId,
            @RequestParam Long equipmentId) {
        TrainingStationResponse response = stationService.bindRiderAndEquipment(stationId, riderId, equipmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}