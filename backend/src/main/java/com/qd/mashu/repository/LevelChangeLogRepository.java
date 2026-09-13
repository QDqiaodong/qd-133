package com.qd.mashu.repository;

import com.qd.mashu.entity.LevelChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelChangeLogRepository extends JpaRepository<LevelChangeLog, Long> {

    List<LevelChangeLog> findByRiderIdOrderByCreateTimeDesc(Long riderId);
}