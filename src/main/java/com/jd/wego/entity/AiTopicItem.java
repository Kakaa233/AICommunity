package com.jd.wego.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 专题-文章关联表
 */
@Data
public class AiTopicItem {
    private int id;
    /** 专题 ID */
    private int topicId;
    /** 文章 ID */
    private int articleId;
    /** 文章与专题的相关度 */
    private double score;
    private Date createdAt;
}
