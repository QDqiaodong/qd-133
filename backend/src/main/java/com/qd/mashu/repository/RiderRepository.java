package com.qd.mashu.repository;

import com.qd.mashu.entity.Rider;
import com.qd.mashu.enums.TrainingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {

    Optional<Rider> findByRiderCode(String riderCode);

    List<Rider> findByCurrentLevel(TrainingLevel currentLevel);

    List<Rider> findByStatus(Integer status);

    List<Rider> findByCurrentLevelAndStatus(TrainingLevel currentLevel, Integer status);

    boolean existsByRiderCode(String riderCode);
}