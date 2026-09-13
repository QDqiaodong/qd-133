package com.qd.mashu.repository;

import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.enums.TrainingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObstacleEquipmentRepository extends JpaRepository<ObstacleEquipment, Long> {

    Optional<ObstacleEquipment> findByEquipmentCode(String equipmentCode);

    List<ObstacleEquipment> findByAdaptLevel(TrainingLevel adaptLevel);

    List<ObstacleEquipment> findByStatus(Integer status);

    List<ObstacleEquipment> findByAdaptLevelAndStatus(TrainingLevel adaptLevel, Integer status);

    boolean existsByEquipmentCode(String equipmentCode);
}