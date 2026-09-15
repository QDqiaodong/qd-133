package com.qd.mashu.repository;

import com.qd.mashu.entity.SessionReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionReviewRepository extends JpaRepository<SessionReview, Long> {

    /** 点评明细：最新提交的排在前面 */
    List<SessionReview> findAllByOrderByCreateTimeDescIdDesc();

    /**
     * 按星级分桶统计条数。汇总与明细查的是同一张表，
     * 各桶条数之和必然等于明细总条数。
     */
    @Query("SELECT r.starRating, COUNT(r) FROM SessionReview r GROUP BY r.starRating")
    List<Object[]> countGroupByStarRating();
}
