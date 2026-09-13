package com.qd.mashu.repository;

import com.qd.mashu.entity.TrainingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingStationRepository extends JpaRepository<TrainingStation, Long> {

    Optional<TrainingStation> findByStationCode(String stationCode);

    List<TrainingStation> findByRiderId(Long riderId);

    List<TrainingStation> findByEquipmentId(Long equipmentId);

    List<TrainingStation> findByStatus(Integer status);

    Optional<TrainingStation> findByRiderIdAndStatus(Long riderId, Integer status);

    boolean existsByStationCode(String stationCode);
}