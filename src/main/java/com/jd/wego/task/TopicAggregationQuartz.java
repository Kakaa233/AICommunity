package com.jd.wego.task;

import com.alibaba.fastjson.JSON;
import com.jd.wego.dao.AiTopicDao;
import com.jd.wego.dao.AiTopicItemDao;
import com.jd.wego.dao.ArticleDao;
import com.jd.wego.entity.Article;
import com.jd.wego.entity.AiTopic;
import com.jd.wego.entity.AiTopicItem;
import com.jd.wego.service.ai.AiGatewayService;
import com.jd.wego.vo.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 专题聚合定时任务 —— 每天凌晨 2 点执行
 * 1. 拉取近 7 天热门文章
 * 2. 调用 AI 服务进行专题聚类
 * 3. 落地到 ai_topic / ai_topic_item 表
 */
@Component
public class TopicAggregationQuartz {

    private static final Logger log = LoggerFactory.getLogger(TopicAggregationQuartz.class);

    @Autowired
    private AiGatewayService aiGatewayService;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private AiTopicDao aiTopicDao;

    @Autowired
    private AiTopicItemDao aiTopicItemDao;

    private static final int TIME_RANGE_DAYS = 7;
    private static final int MAX_TOPICS = 10;

    /**
     * 每天凌晨 2:00 执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateTopics() {
        log.info("TopicAggregationQuartz: starting daily aggregation");

        try {
            // 1. 计算时间范围
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -TIME_RANGE_DAYS);
            Date since = cal.getTime();

            // 2. 拉取近期文章
            List<Article> articles = articleDao.selectRecentArticlesForTopic(since);
            if (articles.isEmpty()) {
                log.info("TopicAggregationQuartz: no recent articles found, skipping");
                return;
            }
            log.info("TopicAggregationQuartz: loaded {} recent articles for topic aggregation", articles.size());

            // 3. 构造请求参数
            List<Map<String, Object>> articleList = articles.stream().map(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", String.valueOf(a.getArticleId()));
                m.put("title", a.getArticleTitle());
                m.put("summary", a.getAiSummary() != null ? a.getAiSummary() : "");
                return m;
            }).collect(Collectors.toList());

            TopicsRequest req = new TopicsRequest();
            req.setTimeRangeDays(TIME_RANGE_DAYS);
            req.setMaxTopics(MAX_TOPICS);
            req.setArticles(articleList);

            // 4. 调用 AI 服务
            AiApiResponse<TopicsResponse> resp = aiGatewayService.callTopics(req);
            if (resp.isFallbackHit() || resp.getData() == null || resp.getData().getTopics() == null) {
                log.warn("TopicAggregationQuartz: AI service returned empty, fallback={}", resp.isFallbackHit());
                return;
            }

            // 5. 落地到数据库
            saveTopics(resp.getData().getTopics(), articleList);

            log.info("TopicAggregationQuartz: completed, {} topics generated", resp.getData().getTopics().size());
        } catch (Exception e) {
            log.error("TopicAggregationQuartz: aggregation failed", e);
        }
    }

    @Transactional
    protected void saveTopics(List<TopicsResponse.TopicItem> topics, List<Map<String, Object>> allArticles) {
        // 构建 articleId -> title 映射
        Map<String, String> idToTitle = allArticles.stream()
                .collect(Collectors.toMap(m -> (String) m.get("id"), m -> (String) m.get("title")));

        for (TopicsResponse.TopicItem item : topics) {
            AiTopic topic = new AiTopic();
            topic.setTitle(item.getTitle());
            topic.setSummary(item.getSummary());
            topic.setScore(item.getScore());
            topic.setCreatedAt(new Date());
            aiTopicDao.insert(topic);

            // 写入关联文章
            if (item.getArticleIds() != null) {
                for (String aid : item.getArticleIds()) {
                    try {
                        AiTopicItem ti = new AiTopicItem();
                        ti.setTopicId(topic.getId());
                        ti.setArticleId(Integer.parseInt(aid));
                        ti.setScore(item.getScore());
                        ti.setCreatedAt(new Date());
                        aiTopicItemDao.insert(ti);
                    } catch (NumberFormatException e) {
                        log.warn("TopicAggregationQuartz: invalid articleId={}", aid);
                    }
                }
            }
        }
    }
}
