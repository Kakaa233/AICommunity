package com.jd.wego.vo.ai;

import lombok.Data;

/**
 * 文章发布前的 AI 处理报告
 */
@Data
public class AiPublishReport {
    /** 内容审核结果 */
    private ModerationResponse moderation;
    /** 审核是否通过（true=合规，false=违规） */
    private boolean moderationPassed = true;
    /** 质量评估 */
    private QualityResponse quality;
    /** 自动生成的摘要 */
    private SummaryResponse summary;
    /** 提取的话题 */
    private TopicsResponse topics;
    /** 熔断器是否已打开（为 true 时所有 AI 结果均为空） */
    private boolean circuitBreakerOpen = false;
}
