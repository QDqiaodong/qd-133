package com.qd.mashu.config;

import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.TrainingStation;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import com.qd.mashu.repository.RiderRepository;
import com.qd.mashu.repository.TrainingStationRepository;
import com.qd.mashu.service.LevelCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private ObstacleEquipmentRepository equipmentRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private TrainingStationRepository stationRepository;

    @Autowired
    private LevelCacheService levelCacheService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing sample data...");

        if (equipmentRepository.count() == 0) {
            createSampleEquipments();
        }

        if (riderRepository.count() == 0) {
            createSampleRiders();
        }

        if (stationRepository.count() == 0) {
            createSampleStations();
        }

        levelCacheService.initializeCache();

        logger.info("Sample data initialization completed");
    }

    private void createSampleEquipments() {
        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ001")
                .equipmentName("初级障碍杆A组")
                .obstacleHeight(40.0)
                .adaptLevel(TrainingLevel.LEVEL_1)
                .description("适合初级骑手训练，高度40cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ002")
                .equipmentName("初级障碍杆B组")
                .obstacleHeight(45.0)
                .adaptLevel(TrainingLevel.LEVEL_1)
                .description("适合初级骑手训练，高度45cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ003")
                .equipmentName("中级障碍杆A组")
                .obstacleHeight(65.0)
                .adaptLevel(TrainingLevel.LEVEL_2)
                .description("适合中级骑手训练，高度65cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ004")
                .equipmentName("中级障碍杆B组")
                .obstacleHeight(75.0)
                .adaptLevel(TrainingLevel.LEVEL_2)
                .description("适合中级骑手训练，高度75cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ005")
                .equipmentName("高级障碍杆A组")
                .obstacleHeight(95.0)
                .adaptLevel(TrainingLevel.LEVEL_3)
                .description("适合高级骑手训练，高度95cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ006")
                .equipmentName("高级障碍杆B组")
                .obstacleHeight(105.0)
                .adaptLevel(TrainingLevel.LEVEL_3)
                .description("适合高级骑手训练，高度105cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ007")
                .equipmentName("专业级障碍杆")
                .obstacleHeight(125.0)
                .adaptLevel(TrainingLevel.LEVEL_4)
                .description("适合专业骑手训练，高度125cm")
                .status(1)
                .build());

        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ008")
                .equipmentName("大师级障碍杆")
                .obstacleHeight(150.0)
                .adaptLevel(TrainingLevel.LEVEL_5)
                .description("适合大师级骑手训练，高度150cm")
                .status(1)
                .build());

        logger.info("Created 8 sample obstacle equipments");
    }

    private void createSampleRiders() {
        riderRepository.save(Rider.builder()
                .riderCode("RD001")
                .riderName("张三")
                .age(25)
                .currentLevel(TrainingLevel.LEVEL_1)
                .phone("13800138001")
                .email("zhangsan@mashu.com")
                .status(1)
                .build());

        riderRepository.save(Rider.builder()
                .riderCode("RD002")
                .riderName("李四")
                .age(30)
                .currentLevel(TrainingLevel.LEVEL_2)
                .phone("13800138002")
                .email("lisi@mashu.com")
                .status(1)
                .build());

        riderRepository.save(Rider.builder()
                .riderCode("RD003")
                .riderName("王五")
                .age(28)
                .currentLevel(TrainingLevel.LEVEL_2)
                .phone("13800138003")
                .email("wangwu@mashu.com")
                .status(1)
                .build());

        riderRepository.save(Rider.builder()
                .riderCode("RD004")
                .riderName("赵六")
                .age(35)
                .currentLevel(TrainingLevel.LEVEL_3)
                .phone("13800138004")
                .email("zhaoliu@mashu.com")
                .status(1)
                .build());

        riderRepository.save(Rider.builder()
                .riderCode("RD005")
                .riderName("钱七")
                .age(22)
                .currentLevel(TrainingLevel.LEVEL_1)
                .phone("13800138005")
                .email("qianqi@mashu.com")
                .status(1)
                .build());

        logger.info("Created 5 sample riders");
    }

    private void createSampleStations() {
        Rider rd001 = riderRepository.findByRiderCode("RD001").orElse(null);
        Rider rd002 = riderRepository.findByRiderCode("RD002").orElse(null);
        Rider rd004 = riderRepository.findByRiderCode("RD004").orElse(null);

        ObstacleEquipment eq001 = equipmentRepository.findByEquipmentCode("EQ001").orElse(null);
        ObstacleEquipment eq003 = equipmentRepository.findByEquipmentCode("EQ003").orElse(null);
        ObstacleEquipment eq005 = equipmentRepository.findByEquipmentCode("EQ005").orElse(null);

        stationRepository.save(TrainingStation.builder()
                .stationCode("ST001")
                .stationName("训练位1号")
                .rider(rd001)
                .equipment(eq001)
                .status(1)
                .build());

        stationRepository.save(TrainingStation.builder()
                .stationCode("ST002")
                .stationName("训练位2号")
                .rider(rd002)
                .equipment(eq003)
                .status(1)
                .build());

        stationRepository.save(TrainingStation.builder()
                .stationCode("ST003")
                .stationName("训练位3号")
                .rider(rd004)
                .equipment(eq005)
                .status(1)
                .build());

        stationRepository.save(TrainingStation.builder()
                .stationCode("ST004")
                .stationName("训练位4号")
                .status(1)
                .build());

        stationRepository.save(TrainingStation.builder()
                .stationCode("ST005")
                .stationName("训练位5号")
                .status(1)
                .build());

        logger.info("Created 5 sample training stations");
    }
}