package com.jd.wego.vo.ai;

import lombok.Data;

/**
 * 内容审核响应
 */
@Data
public class ModerationResponse {
    private boolean isOk;
    private String violationType;
    private String violationExplanation;
}
