package com.qd.mashu.service;

/**
 * 杆高复核业务规则（纯逻辑，便于单元测试）。
 *
 * <p>规则：
 * <ul>
 *     <li>未做杆高复核的杆不能绑上训练位；</li>
 *     <li>实测高度与档案标称高度相差超过约定厘米数（{@link #RECHECK_TOLERANCE_CM}）的，
 *     复核结论为“高度不符”，高度不符的杆必须拦住绑定；</li>
 *     <li>差值不超过约定厘米数的，复核结论为“高度相符”，允许绑定。</li>
 * </ul>
 */
public final class HeightRecheckRules {

    /** 实测与标称允许的最大高度差（cm），俱乐部约定值。超过该值即判定为高度不符。 */
    public static final double RECHECK_TOLERANCE_CM = 5.0;

    /** 复核结论：高度相符。 */
    public static final String RESULT_MATCH = "MATCH";

    /** 复核结论：高度不符。 */
    public static final String RESULT_MISMATCH = "MISMATCH";

    private HeightRecheckRules() {
    }

    /**
     * 判断复核结论。差值“超过”容差才算不符，差值恰好等于容差视为相符。
     *
     * @param nominalHeight  档案标称高度（cm）
     * @param measuredHeight 场上实测高度（cm）
     * @return {@link #RESULT_MATCH} 或 {@link #RESULT_MISMATCH}
     */
    public static String evaluate(double nominalHeight, double measuredHeight) {
        return Math.abs(measuredHeight - nominalHeight) > RECHECK_TOLERANCE_CM
                ? RESULT_MISMATCH : RESULT_MATCH;
    }

    /** 实测减标称的高度差（cm），带正负号。 */
    public static double diff(double nominalHeight, double measuredHeight) {
        return measuredHeight - nominalHeight;
    }

    /** 是否已经完成过杆高复核。 */
    public static boolean isRechecked(Double measuredHeight, String reviewer) {
        return measuredHeight != null && reviewer != null && !reviewer.trim().isEmpty();
    }

    /**
     * 能否绑上训练位。只有“已复核且高度相符”的杆才可以绑定。
     */
    public static boolean canBind(boolean rechecked, String recheckResult) {
        return rechecked && RESULT_MATCH.equals(recheckResult);
    }
}
