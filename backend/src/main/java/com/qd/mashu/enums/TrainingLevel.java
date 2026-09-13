package com.qd.mashu.enums;

public enum TrainingLevel {
    LEVEL_1(1, "初级", "适合入门骑手，障碍高度30-50cm"),
    LEVEL_2(2, "中级", "适合进阶骑手，障碍高度50-80cm"),
    LEVEL_3(3, "高级", "适合熟练骑手，障碍高度80-110cm"),
    LEVEL_4(4, "专业级", "适合专业骑手，障碍高度110-140cm"),
    LEVEL_5(5, "大师级", "适合顶尖骑手，障碍高度140cm以上");

    private final int code;
    private final String name;
    private final String description;

    TrainingLevel(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static TrainingLevel fromCode(int code) {
        for (TrainingLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid training level code: " + code);
    }

    public boolean isCompatibleWith(TrainingLevel riderLevel) {
        return riderLevel.code >= this.code;
    }
}