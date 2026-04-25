package com.jd.wego.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 质量评估响应
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualityResponse {
    private int qualityScore;
    private String suggestions;
    private double totalScore;
    private List<QualityDimension> dimensions;
}
