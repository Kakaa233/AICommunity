package com.jd.wego.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 专题聚合表
 */
@Data
public class AiTopic {
    private int id;
    /** 专题标题 */
    private String title;
    /** 专题摘要 */
    private String summary;
    /** 聚合评分 */
    private double score;
    private Date createdAt;
}
