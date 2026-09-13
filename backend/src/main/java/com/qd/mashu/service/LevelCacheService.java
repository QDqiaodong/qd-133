package com.qd.mashu.service;

import com.qd.mashu.dto.response.LevelCacheTemplate;
import com.qd.mashu.dto.response.LevelEquipmentSummary;
import com.qd.mashu.dto.response.ObstacleEquipmentResponse;
import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LevelCacheService {

    private static final Logger logger = LoggerFactory.getLogger(LevelCacheService.class);

    private static final String LEVEL_CACHE_KEY = "mashu:level_cache_template";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObstacleEquipmentRepository equipmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Map<TrainingLevel, double[]> levelHeightRanges = new HashMap<>();
    static {
        levelHeightRanges.put(TrainingLevel.LEVEL_1, new double[]{30.0, 50.0});
        levelHeightRanges.put(TrainingLevel.LEVEL_2, new double[]{50.0, 80.0});
        levelHeightRanges.put(TrainingLevel.LEVEL_3, new double[]{80.0, 110.0});
        levelHeightRanges.put(TrainingLevel.LEVEL_4, new double[]{110.0, 140.0});
        levelHeightRanges.put(TrainingLevel.LEVEL_5, new double[]{140.0, 200.0});
    }

    public void refreshLevelCache() {
        List<LevelCacheTemplate> templates = new ArrayList<>();

        for (TrainingLevel level : TrainingLevel.values()) {
            double[] range = levelHeightRanges.get(level);
            List<ObstacleEquipment> equipments = equipmentRepository.findByAdaptLevelAndStatus(level, 1);

            List<String> equipmentCodes = equipments.stream()
                    .map(ObstacleEquipment::getEquipmentCode)
                    .collect(Collectors.toList());

            LevelCacheTemplate template = LevelCacheTemplate.builder()
                    .level(level.getCode())
                    .levelName(level.getName())
                    .levelDesc(level.getDescription())
                    .minHeight(range[0])
                    .maxHeight(range[1])
                    .equipmentCodes(equipmentCodes)
                    .build();

            templates.add(template);
        }

        try {
            String json = objectMapper.writeValueAsString(templates);
            redisTemplate.opsForValue().set(LEVEL_CACHE_KEY, json);
            logger.info("Refreshed level cache template in Redis");
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize level cache template", e);
        }
    }

    public List<LevelCacheTemplate> getLevelCacheTemplates() {
        Object cached = redisTemplate.opsForValue().get(LEVEL_CACHE_KEY);
        if (cached != null) {
            try {
                String json = cached.toString();
                return objectMapper.readValue(json, new TypeReference<List<LevelCacheTemplate>>() {});
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize level cache template", e);
            }
        }
        refreshLevelCache();
        return getLevelCacheTemplates();
    }

    public List<LevelEquipmentSummary> getLevelEquipmentSummary() {
        List<LevelEquipmentSummary> summaries = new ArrayList<>();

        for (TrainingLevel level : TrainingLevel.values()) {
            List<ObstacleEquipment> equipments = equipmentRepository.findByAdaptLevelAndStatus(level, 1);

            List<ObstacleEquipmentResponse> equipmentResponses = equipments.stream()
                    .map(ObstacleEquipmentResponse::fromEntity)
                    .collect(Collectors.toList());

            LevelEquipmentSummary summary = LevelEquipmentSummary.builder()
                    .level(level.getCode())
                    .levelName(level.getName())
                    .levelDesc(level.getDescription())
                    .equipmentCount(equipmentResponses.size())
                    .equipments(equipmentResponses)
                    .build();

            summaries.add(summary);
        }

        return summaries;
    }

    public LevelCacheTemplate getTemplateByLevel(Integer level) {
        List<LevelCacheTemplate> templates = getLevelCacheTemplates();
        return templates.stream()
                .filter(t -> t.getLevel().equals(level))
                .findFirst()
                .orElse(null);
    }

    public void initializeCache() {
        if (redisTemplate.opsForValue().get(LEVEL_CACHE_KEY) == null) {
            refreshLevelCache();
            logger.info("Initialized level cache template in Redis");
        }
    }
}