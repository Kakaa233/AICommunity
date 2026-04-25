package com.jd.wego.service.ai;

import com.alibaba.fastjson.JSON;
import com.jd.wego.dao.AiTaskDao;
import com.jd.wego.entity.AiTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AI 异步任务管理服务
 */
@Service
public class AiTaskService {

    private static final Logger log = LoggerFactory.getLogger(AiTaskService.class);

    @Autowired
    private AiTaskDao aiTaskDao;

    /** 默认最大重试次数 */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 创建 AI 异步任务
     */
    public int createTask(String taskType, String bizId, Map<String, Object> payload) {
        AiTask task = new AiTask();
        task.setTaskType(taskType);
        task.setBizId(bizId);
        task.setStatus("pending");
        task.setRetryCount(0);
        task.setMaxRetries(DEFAULT_MAX_RETRIES);
        task.setPayload(JSON.toJSONString(payload));
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());
        aiTaskDao.insert(task);
        log.info("AI task created: type={}, bizId={}, taskId={}", taskType, bizId, task.getId());
        return task.getId();
    }

    /**
     * 标记任务为运行中
     */
    public void markRunning(int taskId) {
        AiTask task = aiTaskDao.selectById(taskId);
        if (task != null) {
            task.setStatus("running");
            task.setUpdatedAt(new Date());
            aiTaskDao.update(task);
        }
    }

    /**
     * 标记任务成功
     */
    public void markSuccess(int taskId, Object result) {
        AiTask task = aiTaskDao.selectById(taskId);
        if (task != null) {
            task.setStatus("success");
            task.setResult(JSON.toJSONString(result));
            task.setUpdatedAt(new Date());
            aiTaskDao.update(task);
            log.info("AI task success: taskId={}", taskId);
        }
    }

    /**
     * 标记任务失败——自动判断是否进入死信
     */
    public void markFailed(int taskId, String errorMsg) {
        AiTask task = aiTaskDao.selectById(taskId);
        if (task != null) {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setErrorMsg(errorMsg);
            task.setUpdatedAt(new Date());

            if (task.getRetryCount() >= task.getMaxRetries()) {
                task.setStatus("dead");
                log.warn("AI task moved to DEAD: taskId={}, retries={}", taskId, task.getRetryCount());
            } else {
                task.setStatus("pending");
                // 指数退避：下次执行时间 = now + 1min * 2^retryCount
                long backoff = (long) Math.pow(2, task.getRetryCount()) * 60 * 1000;
                task.setNextRunAt(new Date(System.currentTimeMillis() + backoff));
                log.info("AI task will retry: taskId={}, retry={}, backoff={}ms",
                        taskId, task.getRetryCount(), backoff);
            }
            aiTaskDao.update(task);
        }
    }

    /**
     * 获取待处理的任务
     */
    public List<AiTask> getPendingTasks(int limit) {
        return aiTaskDao.selectPendingTasks("pending", limit);
    }

    /**
     * 检查某个业务的某个类型任务是否已有 pending 状态的
     */
    public boolean hasPendingTask(String taskType, String bizId) {
        return !aiTaskDao.selectByBizId(taskType, bizId).isEmpty();
    }
}
