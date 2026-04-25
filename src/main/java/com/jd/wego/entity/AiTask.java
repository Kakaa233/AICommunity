package com.jd.wego.entity;

import lombok.Data;
import java.util.Date;

/**
 * AI 异步任务表实体
 */
@Data
public class AiTask {

    private int id;

    /** 任务类型：moderation / topic / recommend / summary */
    private String taskType;

    /** 业务主键（如 article_id） */
    private String bizId;

    /** 任务状态：pending / running / success / failed / dead */
    private String status;

    /** 重试次数 */
    private int retryCount;

    /** 最大重试次数 */
    private int maxRetries;

    /** 请求参数（JSON） */
    private String payload;

    /** 执行结果（JSON） */
    private String result;

    /** 错误信息 */
    private String errorMsg;

    /** 下次执行时间 */
    private Date nextRunAt;

    /** 创建时间 */
    private Date createdAt;

    /** 最后更新时间 */
    private Date updatedAt;
}
