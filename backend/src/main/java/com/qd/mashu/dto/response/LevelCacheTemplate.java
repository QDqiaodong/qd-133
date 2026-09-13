package com.qd.mashu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelCacheTemplate {

    private Integer level;

    private String levelName;

    private String levelDesc;

    private Double minHeight;

    private Double maxHeight;

    private List<String> equipmentCodes;
}