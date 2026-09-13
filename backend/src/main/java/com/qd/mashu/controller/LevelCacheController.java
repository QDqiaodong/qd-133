package com.qd.mashu.controller;

import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.LevelCacheTemplate;
import com.qd.mashu.dto.response.LevelEquipmentSummary;
import com.qd.mashu.service.LevelCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/level")
public class LevelCacheController {

    @Autowired
    private LevelCacheService levelCacheService;

    @GetMapping("/cache")
    public ResponseEntity<ApiResponse<List<LevelCacheTemplate>>> getLevelCacheTemplates() {
        List<LevelCacheTemplate> response = levelCacheService.getLevelCacheTemplates();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cache/{level}")
    public ResponseEntity<ApiResponse<LevelCacheTemplate>> getTemplateByLevel(@PathVariable Integer level) {
        LevelCacheTemplate response = levelCacheService.getTemplateByLevel(level);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/equipment/summary")
    public ResponseEntity<ApiResponse<List<LevelEquipmentSummary>>> getLevelEquipmentSummary() {
        List<LevelEquipmentSummary> response = levelCacheService.getLevelEquipmentSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cache/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshLevelCache() {
        levelCacheService.refreshLevelCache();
        return ResponseEntity.ok(ApiResponse.success("缓存刷新成功", null));
    }
}