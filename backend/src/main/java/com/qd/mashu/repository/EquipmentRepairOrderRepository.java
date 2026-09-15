package com.qd.mashu.repository;

import com.qd.mashu.entity.EquipmentRepairOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepairOrderRepository extends JpaRepository<EquipmentRepairOrder, Long> {

    /** 归还时锁住维修单行，防止重复提交造成后一单覆盖前一单的修复结论。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM EquipmentRepairOrder o WHERE o.id = :id")
    Optional<EquipmentRepairOrder> findWithLockById(@Param("id") Long id);

    /** 同一根杆同时只能有一张在修单。 */
    Optional<EquipmentRepairOrder> findFirstByEquipmentIdAndStatusOrderBySentTimeDesc(Long equipmentId, String status);

    boolean existsByEquipmentIdAndStatus(Long equipmentId, String status);

    List<EquipmentRepairOrder> findByStatusOrderBySentTimeDesc(String status);

    List<EquipmentRepairOrder> findAllByOrderBySentTimeDesc();
}
