package com.qd.mashu.repository;

import com.qd.mashu.entity.Rider;
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
public interface RiderRepository extends JpaRepository<Rider, Long> {

    Optional<Rider> findByRiderCode(String riderCode);

    /**
     * 改级时锁住骑手档案行（SELECT ... FOR UPDATE）：
     * 网络卡顿连点/重试的并发请求会排队等前一单提交，轮到它时看到的已是新等级，
     * 走幂等返回，不会再记一条变更。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Rider r WHERE r.id = :id")
    Optional<Rider> findWithLockById(@Param("id") Long id);

    List<Rider> findByCurrentLevel(TrainingLevel currentLevel);

    List<Rider> findByStatus(Integer status);

    List<Rider> findByCurrentLevelAndStatus(TrainingLevel currentLevel, Integer status);

    boolean existsByRiderCode(String riderCode);
}