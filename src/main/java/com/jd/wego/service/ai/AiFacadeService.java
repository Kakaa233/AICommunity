package com.jd.wego.service.ai;

import com.jd.wego.utils.CodeMsg;
import com.jd.wego.vo.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 业务编排 Facade —— 组合多个 AI 能力完成业务场景
 * <p>
 * 职责：
 * 1. 组合调用（发布文章时：审核 → 质量评估 → 摘要）
 * 2. 熔断保护（连续失败超过阈值则快速失败）
 * 3. 错误翻译（AI 异常 → 业务 CodeMsg）
 */
@Service
public class AiFacadeService {

    private static final Logger log = LoggerFactory.getLogger(AiFacadeService.class);

    @Autowired
    private AiGatewayService aiGatewayService;

    /** 熔断器：连续失败计数 */
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    /** 熔断阈值：连续失败 5 次触发熔断 */
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;

    /** 熔断后恢复正常所需的最小调用间隔（用于计数衰减） */
    private static final int RECOVERY_THRESHOLD = 3;

    // ==================== 熔断器 ====================

    private boolean isCircuitBroken() {
        int failures = consecutiveFailures.get();
        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            log.warn("AI service circuit breaker is OPEN (failures={}), skipping call", failures);
            return true;
        }
        return false;
    }

    private void recordCallResult(boolean success) {
        if (success) {
            int current;
            do {
                current = consecutiveFailures.get();
                if (current <= 0)
                    return;
            } while (!consecutiveFailures.compareAndSet(current, Math.max(0, current - 1)));
        } else {
            consecutiveFailures.incrementAndGet();
            log.warn("AI service failure count incremented to: {}", consecutiveFailures.get());
        }
    }

    // ==================== 文章发布场景 ====================

    /**
     * 文章发布前的 AI 处理管线：
     * 1. 内容审核（Moderation）
     * 2. 质量评估（Quality）
     * 3. 生成摘要（Summary）
     * <p>
     * 所有步骤均为可选的。话题聚合为批量定时任务，不在此处单篇调用。
     */
    public AiPublishReport processArticleBeforePublish(String title, String content) {
        log.info("AiFacade: processing article before publish, title={}, contentLen={}", title, content.length());
        AiPublishReport report = new AiPublishReport();

        if (isCircuitBroken()) {
            report.setCircuitBreakerOpen(true);
            return report;
        }

        // 1. 内容审核
        try {
            ModerationRequest modReq = new ModerationRequest();
            modReq.setContent(title + "\n" + content);
            AiApiResponse<ModerationResponse> modResp = aiGatewayService.callModeration(modReq);
            recordCallResult(!modResp.isFallbackHit());
            if (!modResp.isFallbackHit() && modResp.getData() != null) {
                report.setModeration(modResp.getData());
                report.setModerationPassed(modResp.getData().isOk());
            }
        } catch (Exception e) {
            log.warn("AiFacade: moderation failed, continuing anyway", e);
            recordCallResult(false);
        }

        if (!report.isModerationPassed() && report.getModeration() != null) {
            log.warn("AiFacade: moderation rejected article, violationType={}",
                    report.getModeration().getViolationType());
            return report;
        }

        // 2. 质量评估
        try {
            QualityRequest qReq = new QualityRequest();
            qReq.setContent(content);
            AiApiResponse<QualityResponse> qResp = aiGatewayService.callQuality(qReq);
            recordCallResult(!qResp.isFallbackHit());
            if (!qResp.isFallbackHit() && qResp.getData() != null) {
                report.setQuality(qResp.getData());
            }
        } catch (Exception e) {
            log.warn("AiFacade: quality check failed, continuing anyway", e);
            recordCallResult(false);
        }

        // 3. 生成摘要
        try {
            SummaryRequest sReq = new SummaryRequest();
            sReq.setContent(content);
            AiApiResponse<SummaryResponse> sResp = aiGatewayService.callSummary(sReq);
            recordCallResult(!sResp.isFallbackHit());
            if (!sResp.isFallbackHit() && sResp.getData() != null) {
                report.setSummary(sResp.getData());
            }
        } catch (Exception e) {
            log.warn("AiFacade: summary generation failed, continuing anyway", e);
            recordCallResult(false);
        }

        log.info("AiFacade: article processing completed, summary={}, qualityScore={}",
                report.getSummary() != null ? "generated" : "skipped",
                report.getQuality() != null ? report.getQuality().getQualityScore() : "N/A");
        return report;
    }

    public QualityResponse processArticleOnEdit(String content) {
        log.info("AiFacade: processing article on edit, contentLen={}", content.length());
        if (isCircuitBroken())
            return null;
        try {
            QualityRequest qReq = new QualityRequest();
            qReq.setContent(content);
            AiApiResponse<QualityResponse> qResp = aiGatewayService.callQuality(qReq);
            recordCallResult(!qResp.isFallbackHit());
            return qResp.getData();
        } catch (Exception e) {
            log.warn("AiFacade: quality check on edit failed", e);
            recordCallResult(false);
            return null;
        }
    }

    // ==================== 单步 AI 功能 ====================

    public AiApiResponse<PolishResponse> polish(String text) {
        if (isCircuitBroken()) {
            return aiGatewayService.callAiService("/api/v1/polish", new PolishRequest(), PolishResponse.class);
        }
        PolishRequest req = new PolishRequest();
        req.setText(text);
        AiApiResponse<PolishResponse> resp = aiGatewayService.callPolish(req);
        recordCallResult(!resp.isFallbackHit());
        return resp;
    }

    // public AiApiResponse<DraftResponse> draft(String title, String keywords) {
    // if (isCircuitBroken()) {
    // return aiGatewayService.callAiService("/api/v1/draft", new DraftRequest(),
    // DraftResponse.class);
    // }
    // DraftRequest req = new DraftRequest();
    // req.setTitle(title);
    // req.setKeywords(keywords);
    // AiApiResponse<DraftResponse> resp = aiGatewayService.callDraft(req);
    // recordCallResult(!resp.isFallbackHit());
    // return resp;
    // }

    public AiApiResponse<DraftResponse> draft(String title, String keywords) {
        if (isCircuitBroken()) {
            log.warn("AiFacade: draft skipped because circuit breaker is OPEN");
            return buildDraftFallback("AI service circuit breaker is open");
        }
        if (title == null || title.trim().isEmpty()) {
            log.warn("AiFacade: draft request has empty title, return fallback");
            return buildDraftFallback("title is required");
        }

        DraftRequest req = new DraftRequest();
        req.setTitle(title);
        req.setKeywords(keywords);
        AiApiResponse<DraftResponse> resp = aiGatewayService.callDraft(req);
        recordCallResult(!resp.isFallbackHit());
        return resp;
    }

    private AiApiResponse<DraftResponse> buildDraftFallback(String message) {
        AiApiResponse<DraftResponse> fallback = new AiApiResponse<>();
        fallback.setCode(5002);
        fallback.setMessage(message);
        fallback.setData(null);
        fallback.setLatencyMs(0);
        fallback.setFallbackHit(true);
        fallback.setModel("circuit-open");
        return fallback;
    }

    public AiApiResponse<ModerationResponse> moderate(String content) {
        ModerationRequest req = new ModerationRequest();
        req.setContent(content);
        return aiGatewayService.callModeration(req);
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public void resetCircuitBreaker() {
        consecutiveFailures.set(0);
        log.info("AiFacade: circuit breaker manually reset");
    }
}
