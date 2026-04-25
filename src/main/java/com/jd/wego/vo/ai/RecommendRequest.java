package com.jd.wego.vo.ai;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 相关推荐请求
 */
@Data
public class RecommendRequest {
    private String articleId;
    private int limit = 6;
    private String currentTitle;
    private String currentContent;
    /** 候选文章列表: [{id, title}, ...] */
    private List<Map<String, Object>> candidates;
}
