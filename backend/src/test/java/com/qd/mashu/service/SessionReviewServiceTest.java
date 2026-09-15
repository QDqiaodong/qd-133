package com.qd.mashu.service;

import com.qd.mashu.dto.request.SessionReviewRequest;
import com.qd.mashu.dto.response.SessionReviewResponse;
import com.qd.mashu.dto.response.SessionReviewSummary;
import com.qd.mashu.entity.Rider;
import com.qd.mashu.entity.SessionReview;
import com.qd.mashu.enums.TrainingLevel;
import com.qd.mashu.repository.RiderRepository;
import com.qd.mashu.repository.SessionReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 课后点评核心规则单元测试：三项必填校验、星级一到五、明细与汇总条数一致。
 */
@ExtendWith(MockitoExtension.class)
class SessionReviewServiceTest {

    @Mock
    private SessionReviewRepository reviewRepository;

    @Mock
    private RiderRepository riderRepository;

    @InjectMocks
    private SessionReviewService reviewService;

    private Rider activeRider() {
        return Rider.builder()
                .id(1L)
                .riderCode("RD001")
                .riderName("张三")
                .currentLevel(TrainingLevel.LEVEL_1)
                .status(1)
                .build();
    }

    private SessionReviewRequest request(Long riderId, String focus, Integer stars) {
        return SessionReviewRequest.builder()
                .riderId(riderId)
                .sessionFocus(focus)
                .starRating(stars)
                .build();
    }

    @Test
    void create_normal_review_saved() {
        when(riderRepository.findById(1L)).thenReturn(Optional.of(activeRider()));
        when(reviewRepository.save(any(SessionReview.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionReviewResponse response = reviewService.create(request(1L, "障碍杆节奏与转弯", 4));

        assertEquals("RD001", response.getRider().getRiderCode());
        assertEquals("障碍杆节奏与转弯", response.getSessionFocus());
        assertEquals(4, response.getStarRating());
        verify(reviewRepository, times(1)).save(any(SessionReview.class));
    }

    @Test
    void create_missing_any_of_three_fields_rejected() {
        IllegalArgumentException noRider = assertThrows(IllegalArgumentException.class,
                () -> reviewService.create(request(null, "障碍杆节奏", 4)));
        IllegalArgumentException noFocus = assertThrows(IllegalArgumentException.class,
                () -> reviewService.create(request(1L, "  ", 4)));
        IllegalArgumentException noStars = assertThrows(IllegalArgumentException.class,
                () -> reviewService.create(request(1L, "障碍杆节奏", null)));

        assertTrue(noRider.getMessage().contains("选好骑手"));
        assertTrue(noFocus.getMessage().contains("本节重点"));
        assertTrue(noStars.getMessage().contains("一到五星"));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void create_stars_out_of_one_to_five_rejected() {
        IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
                () -> reviewService.create(request(1L, "障碍杆节奏", 0)));
        IllegalArgumentException six = assertThrows(IllegalArgumentException.class,
                () -> reviewService.create(request(1L, "障碍杆节奏", 6)));

        assertTrue(zero.getMessage().contains("一到五星"));
        assertTrue(six.getMessage().contains("一到五星"));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void create_inactive_rider_rejected() {
        Rider inactive = activeRider();
        inactive.setStatus(0);
        when(riderRepository.findById(1L)).thenReturn(Optional.of(inactive));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewService.create(request(1L, "障碍杆节奏", 4)));

        assertTrue(ex.getMessage().contains("已停用"));
        verify(reviewRepository, never()).save(any(SessionReview.class));
    }

    @Test
    void summary_counts_match_detail_rows_and_fill_zero_buckets() {
        // 4 条明细：两条五星、一条三星、一条一星；二星、四星没评过补 0
        when(reviewRepository.countGroupByStarRating()).thenReturn(List.of(
                new Object[]{5, 2L},
                new Object[]{3, 1L},
                new Object[]{1, 1L}
        ));

        SessionReviewSummary summary = reviewService.summary();

        // 汇总总条数 = 各星级条数之和 = 明细条数
        assertEquals(4, summary.getTotal());
        assertEquals(5, summary.getStarCounts().size());
        assertEquals(2, summary.getStarCounts().get(4).getCount());
        assertEquals(0, summary.getStarCounts().get(3).getCount());
        assertEquals(1, summary.getStarCounts().get(2).getCount());
        assertEquals(0, summary.getStarCounts().get(1).getCount());
        assertEquals(1, summary.getStarCounts().get(0).getCount());
        // (5*2 + 3*1 + 1*1) / 4 = 3.5
        assertEquals(3.5, summary.getAverageStars());
    }

    @Test
    void summary_empty_is_zero() {
        when(reviewRepository.countGroupByStarRating()).thenReturn(List.of());

        SessionReviewSummary summary = reviewService.summary();

        assertEquals(0, summary.getTotal());
        assertEquals(0.0, summary.getAverageStars());
        assertEquals(5, summary.getStarCounts().size());
        assertTrue(summary.getStarCounts().stream().allMatch(c -> c.getCount() == 0));
    }
}
