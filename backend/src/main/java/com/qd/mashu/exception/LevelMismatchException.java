package com.qd.mashu.exception;

public class LevelMismatchException extends RuntimeException {

    public LevelMismatchException(String message) {
        super(message);
    }

    public LevelMismatchException(String riderName, String riderLevel, String equipmentName, String equipmentLevel) {
        super(String.format("等级匹配失败：骑手[%s]当前等级[%s]无法使用障碍设备[%s]，该设备适配等级[%s]",
                riderName, riderLevel, equipmentName, equipmentLevel));
    }
}