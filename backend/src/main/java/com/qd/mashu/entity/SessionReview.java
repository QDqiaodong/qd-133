package com.qd.mashu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课后点评：教练给骑手留的一节课点评（本节重点 + 一到五星）。
 * 点评明细落库持久化，星级汇总按本表实时统计，刷新后明细与汇总都在。
 */
@Entity
@Table(name = "session_review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    /** 本节重点 */
    @Column(name = "session_focus", nullable = false, length = 500)
    private String sessionFocus;

    /** 星级，一到五星 */
    @Column(name = "star_rating", nullable = false)
    private Integer starRating;

    @Column(name = "create_time", nullable = false)
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
