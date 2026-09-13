package com.qd.mashu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderRequest {

    private String riderCode;

    private String riderName;

    private Integer age;

    private Integer currentLevel;

    private String phone;

    private String email;
}