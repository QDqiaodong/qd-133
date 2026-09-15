package com.qd.mashu.repository;

import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.enums.TrainingLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObstacleEquipmentRepository extends JpaRepository<ObstacleEquipment, Long> {

    Optional<ObstacleEquipment> findByEquipmentCode(String equipmentCode);

    /**
     * 绑定/送修/归还共用器材行锁：并发时后提交的一单在锁内复查最新状态，
     * 不能把已经在修的杆重新挂回训练位。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM ObstacleEquipment e WHERE e.id = :id")
    Optional<ObstacleEquipment> findWithLockById(@Param("id") Long id);

    List<ObstacleEquipment> findByAdaptLevel(TrainingLevel adaptLevel);

    List<ObstacleEquipment> findByStatus(Integer status);

    List<ObstacleEquipment> findByAdaptLevelAndStatus(TrainingLevel adaptLevel, Integer status);

    boolean existsByEquipmentCode(String equipmentCode);
}