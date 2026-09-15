package com.qd.mashu.controller;

import com.qd.mashu.dto.request.EquipmentRepairReturnRequest;
import com.qd.mashu.dto.request.EquipmentRepairSendRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.EquipmentRepairOrderResponse;
import com.qd.mashu.service.EquipmentRepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-repair")
public class EquipmentRepairController {

    @Autowired
    private EquipmentRepairService repairService;

    @PostMapping("/equipment/{equipmentId}/send")
    public ResponseEntity<ApiResponse<EquipmentRepairOrderResponse>> sendForRepair(
            @PathVariable Long equipmentId,
            @RequestBody EquipmentRepairSendRequest request) {
        return ResponseEntity.ok(ApiResponse.success(repairService.sendForRepair(equipmentId, request)));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<ApiResponse<EquipmentRepairOrderResponse>> returnFromRepair(
            @PathVariable Long orderId,
            @RequestBody EquipmentRepairReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(repairService.returnFromRepair(orderId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentRepairOrderResponse>>> list(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(repairService.listOrders(status)));
    }
}
