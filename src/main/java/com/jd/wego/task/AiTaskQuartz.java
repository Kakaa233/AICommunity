package com.jd.wego.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.wego.entity.AiTask;
import com.jd.wego.service.ai.*;
import com.jd.wego.vo.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI 异步任务定时调度 —— 轮询 ai_task 表中 pending 状态的任务并执行
 * <p>
 * 每 30 秒轮询一次，每次最多处理 10 个任务
 */
@Component
public class AiTaskQuartz {

    private static final Logger log = LoggerFactory.getLogger(AiTaskQuartz.class);

    @Autowired
    private AiTaskService aiTaskService;

    @Autowired
    private AiGatewayService aiGatewayService;

    /** 每次最多处理 10 个任务 */
    private static final int BATCH_SIZE = 10;

    /**
     * 每 30 秒轮询一次待处理任务
     */
    @Scheduled(fixedRate = 30000)
    public void processPendingTasks() {
        List<AiTask> tasks = aiTaskService.getPendingTasks(BATCH_SIZE);
        if (tasks.isEmpty()) {
            return;
        }
        log.info("AiTaskQuartz: found {} pending tasks", tasks.size());

        for (AiTask task : tasks) {
            try {
                aiTaskService.markRunning(task.getId());
                executeTask(task);
            } catch (Exception e) {
                log.error("AiTaskQuartz: task execution failed, taskId={}", task.getId(), e);
                aiTaskService.markFailed(task.getId(), e.getMessage());
            }
        }
    }

    /**
     * 根据任务类型分发执行
     */
    private void executeTask(AiTask task) {
        Map<String, Object> payload = JSON.parseObject(task.getPayload(),
                new TypeReference<Map<String, Object>>() {});

        switch (task.getTaskType()) {
            case "moderation":
                handleModerationTask(task, payload);
                break;
            case "summary":
                handleSummaryTask(task, payload);
                break;
            case "recommend":
                handleRecommendTask(task, payload);
                break;
            default:
                aiTaskService.markFailed(task.getId(), "Unknown task type: " + task.getTaskType());
        }
    }

    private void handleModerationTask(AiTask task, Map<String, Object> payload) {
        String content = (String) payload.get("content");
        ModerationRequest req = new ModerationRequest();
        req.setContent(content);
        AiApiResponse<ModerationResponse> resp = aiGatewayService.callModeration(req);
        if (!resp.isFallbackHit() && resp.getData() != null) {
            aiTaskService.markSuccess(task.getId(), resp.getData());
        } else {
            aiTaskService.markFailed(task.getId(), resp.getMessage());
        }
    }

    private void handleSummaryTask(AiTask task, Map<String, Object> payload) {
        String content = (String) payload.get("content");
        SummaryRequest req = new SummaryRequest();
        req.setContent(content);
        AiApiResponse<SummaryResponse> resp = aiGatewayService.callSummary(req);
        if (!resp.isFallbackHit() && resp.getData() != null) {
            aiTaskService.markSuccess(task.getId(), resp.getData());
        } else {
            aiTaskService.markFailed(task.getId(), resp.getMessage());
        }
    }

    private void handleRecommendTask(AiTask task, Map<String, Object> payload) {
        String content = (String) payload.get("content");
        RecommendRequest req = new RecommendRequest();
        req.setContent(content);
        AiApiResponse<RecommendResponse> resp = aiGatewayService.callRecommend(req);
        if (!resp.isFallbackHit() && resp.getData() != null) {
            aiTaskService.markSuccess(task.getId(), resp.getData());
        } else {
            aiTaskService.markFailed(task.getId(), resp.getMessage());
        }
    }
}
