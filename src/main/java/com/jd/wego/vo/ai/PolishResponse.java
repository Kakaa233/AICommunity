package com.jd.wego.vo.ai;

import lombok.Data;

/**
 * 文章润色响应
 */
@Data
public class PolishResponse {
    private String polishedText;
    private String changes;
}
