package com.jd.wego.vo.ai;

import lombok.Data;

/**
 * 质量评估维度明细
 */
@Data
public class QualityDimension {
    private String name;
    private double score;
    private String reason;
}
