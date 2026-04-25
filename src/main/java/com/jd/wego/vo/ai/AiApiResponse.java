package com.jd.wego.vo.ai;

import lombok.Data;

/**
 * AI 服务统一 API 响应封装（匹配 Python 端 ApiResponse）
 */
@Data
public class AiApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long latencyMs;
    private boolean fallbackHit;
    private String model;
}
