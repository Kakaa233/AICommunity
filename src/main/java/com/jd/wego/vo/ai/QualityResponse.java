package com.jd.wego.vo.ai;

import lombok.Data;

/**
 * 质量评估响应
 */
@Data
public class QualityResponse {
    private int qualityScore;
    private String suggestions;
}
