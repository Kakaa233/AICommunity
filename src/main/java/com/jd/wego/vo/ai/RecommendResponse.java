package com.jd.wego.vo.ai;

import lombok.Data;
import java.util.List;

/**
 * 相关推荐响应
 */
@Data
public class RecommendResponse {
    private List<RecommendItem> items;

    @Data
    public static class RecommendItem {
        private String articleId;
        private String title;
        private String reason;
        private double score;
    }
}
