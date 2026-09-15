package com.qd.mashu.repository;

import com.qd.mashu.entity.DutyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DutyLogRepository extends JpaRepository<DutyLog, Long> {

    /** 判断某天是否已经写过值班记录，用于保证一天一条 */
    boolean existsByDutyDate(LocalDate dutyDate);

    Optional<DutyLog> findByDutyDate(LocalDate dutyDate);

    /** 值班记录：日期越近越靠前 */
    List<DutyLog> findAllByOrderByDutyDateDescIdDesc();

    /** 按日期翻查当天记录 */
    List<DutyLog> findByDutyDateOrderByDutyDateDescIdDesc(LocalDate dutyDate);
}
