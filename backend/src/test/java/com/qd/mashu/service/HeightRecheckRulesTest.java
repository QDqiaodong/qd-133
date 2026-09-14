package com.qd.mashu.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 杆高复核核心规则单元测试。
 */
class HeightRecheckRulesTest {

    @Test
    void evaluate_boundary_tolerance_is_match() {
        // 差值恰好等于约定 5cm：不算“超过”，判定相符
        assertEquals(HeightRecheckRules.RESULT_MATCH, HeightRecheckRules.evaluate(100.0, 105.0));
        assertEquals(HeightRecheckRules.RESULT_MATCH, HeightRecheckRules.evaluate(100.0, 95.0));
    }

    @Test
    void evaluate_within_tolerance_is_match() {
        assertEquals(HeightRecheckRules.RESULT_MATCH, HeightRecheckRules.evaluate(40.0, 41.0));
        assertEquals(HeightRecheckRules.RESULT_MATCH, HeightRecheckRules.evaluate(40.0, 40.0));
    }

    @Test
    void evaluate_exceeding_tolerance_is_mismatch() {
        assertEquals(HeightRecheckRules.RESULT_MISMATCH, HeightRecheckRules.evaluate(75.0, 85.0));
        assertEquals(HeightRecheckRules.RESULT_MISMATCH, HeightRecheckRules.evaluate(150.0, 142.0));
        // 超出 0.1cm 也算不符
        assertEquals(HeightRecheckRules.RESULT_MISMATCH, HeightRecheckRules.evaluate(100.0, 105.1));
    }

    @Test
    void diff_is_measured_minus_nominal_with_sign() {
        assertEquals(10.0, HeightRecheckRules.diff(75.0, 85.0), 1e-9);
        assertEquals(-8.0, HeightRecheckRules.diff(150.0, 142.0), 1e-9);
    }

    @Test
    void isRechecked_requires_measured_height_and_reviewer() {
        assertFalse(HeightRecheckRules.isRechecked(null, "陈教练"));
        assertFalse(HeightRecheckRules.isRechecked(40.0, null));
        assertFalse(HeightRecheckRules.isRechecked(40.0, "  "));
        assertTrue(HeightRecheckRules.isRechecked(41.0, "陈教练"));
    }

    @Test
    void canBind_only_rechecked_match() {
        assertFalse(HeightRecheckRules.canBind(false, null));
        assertFalse(HeightRecheckRules.canBind(true, HeightRecheckRules.RESULT_MISMATCH));
        assertTrue(HeightRecheckRules.canBind(true, HeightRecheckRules.RESULT_MATCH));
    }
}
