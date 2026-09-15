package com.qd.mashu.config;

import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.TrainingStation;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.ObstacleEquipmentRepository;
import com.qd.mashu.repository.RiderRepository;
import com.qd.mashu.repository.TrainingStationRepository;
import com.qd.mashu.service.HeightRecheckRules;
import com.qd.mashu.service.LevelCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        // 已复核且高度相符（差值在约定 5cm 以内），可以绑上训练位
        saveEquipmentWithRecheck("EQ001", "初级障碍杆A组", 40.0, TrainingLevel.LEVEL_1,
                "适合初级骑手训练，高度40cm", 41.0, "陈教练");
        saveEquipmentWithRecheck("EQ002", "初级障碍杆B组", 45.0, TrainingLevel.LEVEL_1,
                "适合初级骑手训练，高度45cm", 45.0, "陈教练");
        // 已复核且高度相符
        saveEquipmentWithRecheck("EQ003", "中级障碍杆A组", 65.0, TrainingLevel.LEVEL_2,
                "适合中级骑手训练，高度65cm", 68.0, "刘教练");
        // 已复核但高度不符（实测比标称高 10cm），不能绑上训练位
        saveEquipmentWithRecheck("EQ004", "中级障碍杆B组", 75.0, TrainingLevel.LEVEL_2,
                "适合中级骑手训练，高度75cm", 85.0, "刘教练");
        // 已复核且高度相符
        saveEquipmentWithRecheck("EQ005", "高级障碍杆A组", 95.0, TrainingLevel.LEVEL_3,
                "适合高级骑手训练，高度95cm", 94.0, "周教练");
        // 未做杆高复核，不能绑上训练位
        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ006")
                .equipmentName("高级障碍杆B组")
                .obstacleHeight(105.0)
                .adaptLevel(TrainingLevel.LEVEL_3)
                .description("适合高级骑手训练，高度105cm")
                .status(1)
                .build());
        // 未做杆高复核，不能绑上训练位
        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode("EQ007")
                .equipmentName("专业级障碍杆")
                .obstacleHeight(125.0)
                .adaptLevel(TrainingLevel.LEVEL_4)
                .description("适合专业骑手训练，高度125cm")
                .status(1)
                .build());
        // 已复核但高度不符（实测比标称低 8cm），不能绑上训练位
        saveEquipmentWithRecheck("EQ008", "大师级障碍杆", 150.0, TrainingLevel.LEVEL_5,
                "适合大师级骑手训练，高度150cm", 142.0, "周教练");

        logger.info("Created 8 sample obstacle equipments (with height recheck states)");
    }

    /**
     * 按规则计算复核结论后落库，保证样例数据的复核结论与高度差、绑定资格始终一致。
     */
    private void saveEquipmentWithRecheck(String code, String name, double nominalHeight,
                                          TrainingLevel level, String description,
                                          double measuredHeight, String reviewer) {
        String result = HeightRecheckRules.evaluate(nominalHeight, measuredHeight);
        equipmentRepository.save(ObstacleEquipment.builder()
                .equipmentCode(code)
                .equipmentName(name)
                .obstacleHeight(nominalHeight)
                .adaptLevel(level)
                .description(description)
                .status(1)
                .measuredHeight(measuredHeight)
                .recheckReviewer(reviewer)
                .recheckResult(result)
                .recheckHeightDiff(HeightRecheckRules.diff(nominalHeight, measuredHeight))
                .recheckTime(LocalDateTime.now())
                .build());
    }

    private void createSampleRiders() {
        riderRepository.save(Rider.builder()
                .riderCode("RD001")
                .riderName("张三")
                .age(25)
                .currentLevel(TrainingLevel.LEVEL_1)
                .phone("13800138001")
                .email("zhangsan@mashu.com")
                .lastFitnessTestDate(LocalDate.now().minusDays(20))
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
                .lastFitnessTestDate(LocalDate.now().minusDays(5))
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