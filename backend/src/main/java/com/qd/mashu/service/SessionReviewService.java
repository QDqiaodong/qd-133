package com.qd.mashu.service;

import com.qd.mashu.dto.request.SessionReviewRequest;
import com.qd.mashu.dto.response.SessionReviewResponse;
import com.qd.mashu.dto.response.SessionReviewSummary;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.SessionReview;
import com.qd.mashu.repository.RiderRepository;
import com.qd.mashu.repository.SessionReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课后点评服务：教练选好骑手，写下本节重点和一到五星后提交。
 *
 * <p>点评明细落库持久化；星级汇总按同一张表实时 GROUP BY 统计，
 * 明细条数与汇总条数同源，必然保持一致，刷新后两处都还在。
 */
@Service
public class SessionReviewService {

    private static final Logger logger = LoggerFactory.getLogger(SessionReviewService.class);

    /** 最低一星 */
    public static final int MIN_STARS = 1;

    /** 最高五星 */
    public static final int MAX_STARS = 5;

    @Autowired
    private SessionReviewRepository reviewRepository;

    @Autowired
    private RiderRepository riderRepository;

    /**
     * 提交课后点评：骑手、本节重点、一到五星三项缺一不可。
     */
    @Transactional
    public SessionReviewResponse create(SessionReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("点评信息不能为空");
        }
        if (request.getRiderId() == null) {
            throw new IllegalArgumentException("请先选好骑手");
        }
        if (request.getSessionFocus() == null || request.getSessionFocus().trim().isEmpty()) {
            throw new IllegalArgumentException("请写下本节重点");
        }
        if (request.getStarRating() == null
                || request.getStarRating() < MIN_STARS
                || request.getStarRating() > MAX_STARS) {
            throw new IllegalArgumentException("请选择一到五星的星级");
        }

        Rider rider = riderRepository.findById(request.getRiderId())
                .orElseThrow(() -> new IllegalArgumentException("骑手不存在"));
        if (rider.getStatus() == null || rider.getStatus() != 1) {
            throw new IllegalArgumentException("骑手已停用，不能点评");
        }

        SessionReview review = SessionReview.builder()
                .rider(rider)
                .sessionFocus(request.getSessionFocus().trim())
                .starRating(request.getStarRating())
                .build();

        review = reviewRepository.save(review);
        logger.info("Created session review: rider[{}] stars={} focus={}",
                rider.getRiderCode(), review.getStarRating(), review.getSessionFocus());

        return SessionReviewResponse.fromEntity(review);
    }

    /**
     * 点评明细：全部点评，最新提交的排在前面。
     */
    public List<SessionReviewResponse> listAll() {
        return reviewRepository.findAllByOrderByCreateTimeDescIdDesc().stream()
                .map(SessionReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 按星级统计汇总：一到五星固定 5 桶，没评过的星级补 0。
     * 总条数取各桶之和，与点评明细查的是同一张表，两处条数保持一致。
     */
    public SessionReviewSummary summary() {
        Map<Integer, Long> countByStars = new HashMap<>();
        for (Object[] row : reviewRepository.countGroupByStarRating()) {
            countByStars.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        List<SessionReviewSummary.StarCount> starCounts = new ArrayList<>();
        long total = 0;
        long starSum = 0;
        for (int stars = MIN_STARS; stars <= MAX_STARS; stars++) {
            long count = countByStars.getOrDefault(stars, 0L);
            starCounts.add(SessionReviewSummary.StarCount.builder()
                    .stars(stars)
                    .count(count)
                    .build());
            total += count;
            starSum += (long) stars * count;
        }

        double averageStars = total == 0 ? 0.0 : Math.round(starSum * 10.0 / total) / 10.0;

        return SessionReviewSummary.builder()
                .total(total)
                .averageStars(averageStars)
                .starCounts(starCounts)
                .build();
    }
}
