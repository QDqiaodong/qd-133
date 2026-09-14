package com.qd.mashu.dto.response;

import com.qd.mashu.entity.ObstacleEquipment;
import com.qd.mashu.service.HeightRecheckRules;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleEquipmentResponse {

    private Long id;

    private String equipmentCode;

    private String equipmentName;

    private Double obstacleHeight;

    private Integer adaptLevel;

    private String adaptLevelName;

    private String adaptLevelDesc;

    private String description;

    private Integer status;

    // ===== 杆高复核信息 =====

    private Double measuredHeight;

    private String recheckReviewer;

    /** 复核结论编码：MATCH / MISMATCH；未复核为 null */
    private String recheckResult;

    /** 复核结论中文名：高度相符 / 高度不符；未复核为 null */
    private String recheckResultName;

    private Double recheckHeightDiff;

    private LocalDateTime recheckTime;

    /** 是否已做杆高复核 */
    private Boolean rechecked;

    /** 复核结论与当前绑定资格是否允许绑上训练位：只有“已复核且高度相符”才可绑定 */
    private Boolean bindable;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static ObstacleEquipmentResponse fromEntity(ObstacleEquipment entity) {
        boolean rechecked = HeightRecheckRules.isRechecked(entity.getMeasuredHeight(), entity.getRecheckReviewer());
        boolean bindable = HeightRecheckRules.canBind(rechecked, entity.getRecheckResult());
        return ObstacleEquipmentResponse.builder()
                .id(entity.getId())
                .equipmentCode(entity.getEquipmentCode())
                .equipmentName(entity.getEquipmentName())
                .obstacleHeight(entity.getObstacleHeight())
                .adaptLevel(entity.getAdaptLevel().getCode())
                .adaptLevelName(entity.getAdaptLevel().getName())
                .adaptLevelDesc(entity.getAdaptLevel().getDescription())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .measuredHeight(entity.getMeasuredHeight())
                .recheckReviewer(entity.getRecheckReviewer())
                .recheckResult(entity.getRecheckResult())
                .recheckResultName(resolveResultName(entity.getRecheckResult()))
                .recheckHeightDiff(entity.getRecheckHeightDiff())
                .recheckTime(entity.getRecheckTime())
                .rechecked(rechecked)
                .bindable(bindable)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private static String resolveResultName(String result) {
        if (HeightRecheckRules.RESULT_MATCH.equals(result)) {
            return "高度相符";
        }
        if (HeightRecheckRules.RESULT_MISMATCH.equals(result)) {
            return "高度不符";
        }
        return null;
    }
}