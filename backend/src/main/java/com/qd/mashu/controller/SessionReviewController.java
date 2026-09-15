package com.qd.mashu.controller;

import com.qd.mashu.dto.request.SessionReviewRequest;
import com.qd.mashu.dto.response.ApiResponse;
import com.qd.mashu.dto.response.SessionReviewResponse;
import com.qd.mashu.dto.response.SessionReviewSummary;
import com.qd.mashu.service.SessionReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课后点评：教练选好骑手，写下本节重点和一到五星后提交；
 * 明细与星级汇总同表同源，条数保持一致。
 */
@RestController
@RequestMapping("/api/review")
public class SessionReviewController {

    @Autowired
    private SessionReviewService reviewService;

    /** 提交一条课后点评（骑手 + 本节重点 + 一到五星）。 */
    @PostMapping
    public ResponseEntity<ApiResponse<SessionReviewResponse>> create(@RequestBody SessionReviewRequest request) {
        SessionReviewResponse response = reviewService.create(request);
        return ResponseEntity.ok(ApiResponse.success("点评提交成功", response));
    }

    /** 点评明细列表，最新提交的排在前面。 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionReviewResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.listAll()));
    }

    /** 按星级统计汇总：总条数、平均星级、一到五星各桶条数。 */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SessionReviewSummary>> summary() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.summary()));
    }
}
