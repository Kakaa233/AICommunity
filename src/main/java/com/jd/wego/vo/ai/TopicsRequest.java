package com.jd.wego.vo.ai;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 专题聚合请求
 */
@Data
public class TopicsRequest {
    private int timeRangeDays = 7;
    private int maxTopics = 10;
    /** 文章列表: [{id, title, summary}, ...] */
    private List<Map<String, Object>> articles;
}
