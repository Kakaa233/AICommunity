package com.jd.wego.vo.ai;

import lombok.Data;
import java.util.List;

/**
 * 专题聚合响应
 */
@Data
public class TopicsResponse {
    private List<TopicItem> topics;

    @Data
    public static class TopicItem {
        private String topicId;
        private String title;
        private String summary;
        private List<String> articleIds;
        private double score;
    }
}
