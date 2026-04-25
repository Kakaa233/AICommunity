package com.jd.wego.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * 7
 *
 * @author hbquan
 * @date 2021/4/6 14:39
 */
@Data
@Document(indexName = "wego", type = "_doc", createIndex = false)
public class Article {


    @Id
    @Field(type = FieldType.Integer, name = "article_id")
    private int articleId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", name = "article_title", searchAnalyzer = "ik_max_word")
    private String articleTitle;

    @Field(type = FieldType.Text, name = "article_summary")
    private String articleSummary;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", name = "article_content", searchAnalyzer = "ik_max_word")
    private String articleContent;

    @Field(type = FieldType.Integer, name = "article_view_count")
    private int articleViewCount;

    @Field(type = FieldType.Integer, name = "article_like_count")
    private int articleLikeCount;

    @Field(type = FieldType.Integer, name = "article_comment_count")
    private int articleCommentCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Field(type = FieldType.Date, name = "created_time", format = DateFormat.date_optional_time)
    private Date createdTime;

    /**
     *
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Field(type = FieldType.Date, name = "update_time", format = DateFormat.date_optional_time)
    private Date updateTime;

    /**
     * isDelete表示两种状态，0表示未删除，1表示已删除
     */
    @Field(type = FieldType.Integer, name = "is_deleted")
    private int isDeleted;

    @Field(type = FieldType.Integer, name = "article_category_id")
    private int articleCategoryId;

    @Field(type = FieldType.Text, name = "article_category_name")
    private String articleCategoryName;

    @Field(type = FieldType.Text, name = "article_user_id")
    private String articleUserId;

    // ==================== AI 辅助字段 ====================

    /** AI 自动生成的摘要 */
    @Field(type = FieldType.Text, name = "ai_summary")
    private String aiSummary;

    /** AI 质量评分（0-100） */
    @Field(type = FieldType.Integer, name = "ai_quality_score")
    private Integer aiQualityScore;

    /** AI 审核状态：pass / flag / reject / pending */
    @Field(type = FieldType.Text, name = "ai_review_status")
    private String aiReviewStatus;

    /** AI 审核原因/违规说明 */
    @Field(type = FieldType.Text, name = "ai_review_reason")
    private String aiReviewReason;

    /** AI 提取的话题标签（JSON 数组） */
    @Field(type = FieldType.Text, name = "ai_tags_json")
    private String aiTagsJson;

    /** 是否允许 AI 辅助 */
    @Field(type = FieldType.Integer, name = "ai_enabled")
    private Boolean aiEnabled;
}