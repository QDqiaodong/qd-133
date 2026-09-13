package com.qd.mashu.dto.response;

import com.qd.mashu.entity.Rider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderResponse {

    private Long id;

    private String riderCode;

    private String riderName;

    private Integer age;

    private Integer currentLevel;

    private String currentLevelName;

    private String currentLevelDesc;

    private String phone;

    private String email;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static RiderResponse fromEntity(Rider entity) {
        return RiderResponse.builder()
                .id(entity.getId())
                .riderCode(entity.getRiderCode())
                .riderName(entity.getRiderName())
                .age(entity.getAge())
                .currentLevel(entity.getCurrentLevel().getCode())
                .currentLevelName(entity.getCurrentLevel().getName())
                .currentLevelDesc(entity.getCurrentLevel().getDescription())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}