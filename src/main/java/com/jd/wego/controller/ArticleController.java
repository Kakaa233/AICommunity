package com.jd.wego.controller;

import com.jd.wego.entity.Article;
import com.jd.wego.entity.Category;
import com.jd.wego.entity.User;
import com.jd.wego.redis.JedisService;
import com.jd.wego.service.ArticleService;
import com.jd.wego.service.CategoryService;
import com.jd.wego.service.UserService;
import com.jd.wego.service.ai.AiFacadeService;
import com.jd.wego.service.ai.AiGatewayService;
import com.jd.wego.service.ai.AiTaskService;
import com.jd.wego.utils.CodeMsg;
import com.jd.wego.utils.Result;
import com.jd.wego.vo.ArticleUserVo;
import com.jd.wego.vo.ai.AiPublishReport;
import com.jd.wego.vo.ai.PolishRequest;
import com.jd.wego.vo.ai.PolishResponse;
import com.jd.wego.vo.ai.DraftRequest;
import com.jd.wego.vo.ai.DraftResponse;
import com.jd.wego.vo.ai.ModerationRequest;
import com.jd.wego.vo.ai.ModerationResponse;
import com.jd.wego.vo.ai.QualityRequest;
import com.jd.wego.vo.ai.QualityResponse;
import com.jd.wego.vo.ai.RecommendRequest;
import com.jd.wego.vo.ai.RecommendResponse;
import com.jd.wego.vo.ai.SummaryResponse;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hbquan
 * @date 2021/4/7 15:54
 * <p>
 * 设计：
 * 当用户发表一篇文章的时候，成就值+10分
 * 当用户的文章被别人点赞一次之后，成就值+5分
 * 当用户的文章被别人评论一次之后，成就值+5分
 * 当用户被一个人关注后，成就值+10分
 */
@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    ArticleService articleService;

    @Autowired
    JedisService jedisService;

    @Autowired
    LoginController loginController;

    @Autowired
    CategoryService categoryService;

    @Autowired
    UserService userService;

    @Autowired
    AiFacadeService aiFacadeService;

    @Autowired
    AiGatewayService aiGatewayService;

    @Autowired
    AiTaskService aiTaskService;

    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    @PostMapping("/insert")
    @ResponseBody
    public Result<Map<String, Object>> insertArticle(HttpServletRequest request, @RequestBody Article article) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            log.info("用户未登录");
            return Result.error(CodeMsg.NOT_LOGIN);
        }

        log.info(article.toString());
        // 需要通过闯过来的categoryName来查找出categoryId
        Category category = categoryService.selectCategoryByName(article.getArticleCategoryName());
        log.info("category对象为：{}", category.toString());

        article.setArticleCategoryId(category.getCategoryId());
        article.setCreatedTime(new Date());
        article.setUpdateTime(new Date());
        article.setArticleUserId(user.getUserId());

        // 发表一篇文章用户的成就值+10分
        User publishUser = userService.selectByUserId(user.getUserId());
        publishUser.setAchieveValue(publishUser.getAchieveValue() + 10);
        userService.updateByUserId(publishUser);
        articleService.insertArticle(article);

        // ===== AI 处理管线（异步非阻塞，失败不影响发布） =====
        Map<String, Object> aiReportMap = new HashMap<>();
        try {
            AiPublishReport aiReport = aiFacadeService.processArticleBeforePublish(
                    article.getArticleTitle(), article.getArticleContent());

            // 收集需要更新的 AI 字段
            boolean aiFieldsUpdated = false;

            // 如果有自动摘要，写入 ai_summary
            if (aiReport.getSummary() != null && aiReport.getSummary().getSummary() != null
                    && !aiReport.getSummary().getSummary().isEmpty()) {
                String aiSummary = aiReport.getSummary().getSummary();
                if (aiSummary.length() > 200) {
                    aiSummary = aiSummary.substring(0, 200);
                }
                article.setAiSummary(aiSummary);
                aiFieldsUpdated = true;
                log.info("AI摘要已生成 articleId={}", article.getArticleId());
                aiReportMap.put("summary", aiSummary);
            }

            // 质量评分写入 ai_quality_score
            if (aiReport.getQuality() != null) {
                article.setAiQualityScore(aiReport.getQuality().getQualityScore());
                aiFieldsUpdated = true;
                log.info("AI质量评分: {}，建议: {}", aiReport.getQuality().getQualityScore(),
                        aiReport.getQuality().getSuggestions());
                aiReportMap.put("qualityScore", aiReport.getQuality().getQualityScore());
                aiReportMap.put("suggestions", aiReport.getQuality().getSuggestions());
            }

            // 审核结果写入 ai_review_status / ai_review_reason
            if (aiReport.getModeration() != null) {
                if (aiReport.getModeration().isOk()) {
                    article.setAiReviewStatus("pass");
                    aiReportMap.put("reviewStatus", "pass");
                } else {
                    article.setAiReviewStatus("flag");
                    article.setAiReviewReason(aiReport.getModeration().getViolationExplanation());
                    log.warn("AI内容审核警告: 文章 articleId={} 可能存在违规: {}",
                            article.getArticleId(), aiReport.getModeration().getViolationExplanation());
                    aiReportMap.put("reviewStatus", "flag");
                    aiReportMap.put("reviewReason", aiReport.getModeration().getViolationExplanation());
                }
                aiFieldsUpdated = true;
            } else {
                // 审核无结果，标记为 pending 等待异步补充
                article.setAiReviewStatus("pending");
                aiFieldsUpdated = true;
                aiReportMap.put("reviewStatus", "pending");
            }

            // 话题标签写入 ai_tags_json
            if (aiReport.getTopics() != null && aiReport.getTopics().getTopics() != null
                    && !aiReport.getTopics().getTopics().isEmpty()) {
                article.setAiTagsJson(JSON.toJSONString(aiReport.getTopics().getTopics()));
                aiFieldsUpdated = true;
            }

            // 如果有 AI 字段更新，同步到数据库
            if (aiFieldsUpdated) {
                article.setUpdateTime(new Date());
                articleService.updateArticle(article);
                log.info("AI处理结果已落库 articleId={}", article.getArticleId());
            }

            // ===== 创建异步任务（补全未完成的 AI 处理） =====
            // 如果没有摘要，创建异步摘要任务
            if (aiReport.getSummary() == null || aiReport.getSummary().getSummary() == null) {
                Map<String, Object> summaryPayload = new HashMap<>();
                summaryPayload.put("content", article.getArticleContent());
                summaryPayload.put("articleId", String.valueOf(article.getArticleId()));
                aiTaskService.createTask("summary", String.valueOf(article.getArticleId()), summaryPayload);
            }

            // 如果没有审核结果，创建异步审核任务
            if (aiReport.getModeration() == null) {
                Map<String, Object> modPayload = new HashMap<>();
                modPayload.put("content", article.getArticleTitle() + "\n" + article.getArticleContent());
                modPayload.put("articleId", String.valueOf(article.getArticleId()));
                aiTaskService.createTask("moderation", String.valueOf(article.getArticleId()), modPayload);
            }
        } catch (Exception e) {
            // AI 任何异常都不影响主流程
            log.warn("AI处理后处理异常，已忽略: {}", e.getMessage());
        }

        return Result.success(aiReportMap);
    }

    @GetMapping("/can/edit")
    @ResponseBody
    public Result<Boolean> canEditArticle(HttpServletRequest request, int articleId) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            log.info("用户未登录");
            return Result.error(CodeMsg.NOT_LOGIN);
        }
        Article article = articleService.selectArticleByArticleId(articleId);
        if (article.getArticleUserId().equals(user.getUserId())) {
            // 是发表文章的作者，才有权更新文章
            return Result.success(true);
        } else {
            return Result.success(false);
        }
    }

    @PostMapping("/edit")
    @ResponseBody
    public Result<Boolean> editArticle(@RequestBody Article article) {
        articleService.updateArticle(article);
        return Result.success(true);
    }

    @GetMapping("/delete")
    @ResponseBody
    public Result<Boolean> deleteArticle(HttpServletRequest request, int articleId) {
        User user = loginController.getUserInfo(request);
        if (user == null) {
            log.info("用户未登录");
            return Result.error(CodeMsg.NOT_LOGIN);
        }
        Article article = articleService.selectArticleByArticleId(articleId);
        if (article.getArticleUserId().equals(user.getUserId())) {
            // 是发表文章的作者，才能有权操作删除文章
            articleService.deleteArticle(articleId);
            return Result.success(true);
        } else {
            return Result.success(false);
        }

    }


    @GetMapping("/search")
    @ResponseBody
    public Result<List<ArticleUserVo>> searchArticle(String keyword) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 该方法是调用ElasticSearch的接口来查询的
        List<ArticleUserVo> articleList = articleService.selectArticleByKeywords(keyword);
        stopWatch.stop();
        log.info("使用ES来搜索文章的耗时为：{}ms", stopWatch.getTotalTimeMillis());
        return Result.success(articleList);
    }

    /**
     * 这个代码并无和业务代码相关，是为了测试下同等情况下和ES的查询效率哪个更高
     *
     * @param keyword
     * @return
     */
    @GetMapping("/search/mysql")
    @ResponseBody
    public Result<List<ArticleUserVo>> searchArticleByMysql(String keyword) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 该方法是调用ElasticSearch的接口来查询的
        List<ArticleUserVo> articleList = articleService.selectArticleByKeyword(keyword);
        stopWatch.stop();
        log.info("使用Mysql的模糊查询来搜索文章的耗时为：{}ms", stopWatch.getTotalTimeMillis());
        return Result.success(articleList);
    }


    /**
     * 展示10条热点文章，热点文章是根据文章的浏览量来进行排序
     * @return
     */
    @GetMapping("/hotspot")
    @ResponseBody
    public Result<List<ArticleUserVo>> hostSpotArticle() {
        List<ArticleUserVo> articleList = articleService.selectArticleByViewCount();
        return Result.success(articleList);
    }

    @GetMapping("/select/school/category/{categoryId}")
    @ResponseBody
    public Result<List<ArticleUserVo>> SameSchoolArticle(HttpServletRequest request, @PathVariable("categoryId") int categoryId) {
        /**
         * 这里又发现一个小bug,原因是：在更新用户个人信息之后，只是更改了数据库的信息，但是并没有更改redis
         * 中的token值，这样就导致了直接从redis中读取的数据是历史数据，因此就会出现逻辑错误
         */

        User user = loginController.getUserInfo(request);
        log.info("userInfo：{}",user.toString());
        // 这里通过用户获取学校信息，由于历史问题出现错误，现在先将其写死为西安科技大学
        String schoolName = user.getSchool();
        log.info("schoolName is :{}", schoolName);

        // 写成这样进行测试的话，前端仍然无法根据具体的分类进行展示数据，只能对第一个数据进行展示？？
        List<ArticleUserVo> articleUserVoList = articleService.selectArticleBySchool(schoolName,categoryId);
        /*
        for(ArticleUserVo articleUserVo : articleUserVoList){
            log.info("articleUserVo:{}", articleUserVo.toString() + "userSchool:" + user.getSchool());
        }
        */
        return Result.success(articleUserVoList);
    }

    // ==================== AI 辅助接口 ====================

    /**
     * AI 润色文章
     */
    @PostMapping("/ai/polish")
    @ResponseBody
    public Result<PolishResponse> aiPolish(@RequestBody PolishRequest request) {
        if (request.getText() == null || request.getText().isEmpty()) {
            return Result.error(new CodeMsg(400, "文本不能为空"));
        }
        try {
            com.jd.wego.vo.ai.AiApiResponse<PolishResponse> resp = aiFacadeService.polish(request.getText());
            if (resp.getData() != null) {
                return Result.success(resp.getData());
            } else {
                return Result.error(new CodeMsg(500, "AI润色服务暂不可用，请稍后重试"));
            }
        } catch (Exception e) {
            log.error("AI polish error", e);
            return Result.error(new CodeMsg(500, "AI润色服务异常"));
        }
    }

    /**
     * AI 写稿
     */
    @PostMapping("/ai/draft")
    @ResponseBody
    public Result<DraftResponse> aiDraft(@RequestBody DraftRequest request) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return Result.error(new CodeMsg(400, "标题不能为空"));
        }
        try {
            com.jd.wego.vo.ai.AiApiResponse<DraftResponse> resp = aiFacadeService.draft(
                    request.getTitle(), request.getKeywords());
            if (resp.getData() != null) {
                return Result.success(resp.getData());
            } else {
                return Result.error(new CodeMsg(500, "AI写稿服务暂不可用，请稍后重试"));
            }
        } catch (Exception e) {
            log.error("AI draft error", e);
            return Result.error(new CodeMsg(500, "AI写稿服务异常"));
        }
    }

    /**
     * 内容审核
     */
    @PostMapping("/ai/moderate")
    @ResponseBody
    public Result<ModerationResponse> aiModerate(@RequestBody ModerationRequest request) {
        if (request.getContent() == null || request.getContent().isEmpty()) {
            return Result.error(new CodeMsg(400, "内容不能为空"));
        }
        try {
            com.jd.wego.vo.ai.AiApiResponse<ModerationResponse> resp = aiFacadeService.moderate(request.getContent());
            if (resp.getData() != null) {
                return Result.success(resp.getData());
            } else {
                return Result.error(new CodeMsg(500, "内容审核服务暂不可用"));
            }
        } catch (Exception e) {
            log.error("AI moderate error", e);
            return Result.error(new CodeMsg(500, "内容审核服务异常"));
        }
    }

    /**
     * AI 质量评估 — 写文章页预览调用
     */
    @PostMapping("/ai/quality")
    @ResponseBody
    public Result<QualityResponse> aiQuality(@RequestBody QualityRequest request) {
        if (request.getContent() == null || request.getContent().isEmpty()) {
            return Result.error(new CodeMsg(400, "内容不能为空"));
        }
        try {
            QualityResponse data = aiFacadeService.processArticleOnEdit(request.getContent());
            if (data != null) {
                return Result.success(data);
            } else {
                return Result.error(new CodeMsg(500, "质量评估服务暂不可用"));
            }
        } catch (Exception e) {
            log.error("AI quality error", e);
            return Result.error(new CodeMsg(500, "质量评估服务异常"));
        }
    }

    /**
     * 智能推荐 —— 根据文章 ID 获取相关推荐（详情页调用）
     * GET /article/ai/recommend?articleId=123
     */
    @GetMapping("/ai/recommend")
    @ResponseBody
    public Result<RecommendResponse> aiRecommend(@RequestParam int articleId) {
        try {
            // 1. 获取当前文章
            Article article = articleService.selectArticleByArticleId(articleId);
            if (article == null) {
                return Result.error(new CodeMsg(400, "文章不存在"));
            }

            // 2. 构建请求
            RecommendRequest req = new RecommendRequest();
            req.setArticleId(String.valueOf(articleId));
            req.setCurrentTitle(article.getArticleTitle());
            String content = article.getArticleContent();
            req.setCurrentContent(content != null && content.length() > 500 ? content.substring(0, 500) : content);

            // 3. 从热点文章选取候选（按浏览量排序，排除当前文章）
            List<ArticleUserVo> hotArticles = articleService.selectArticleByViewCount();
            if (hotArticles != null && !hotArticles.isEmpty()) {
                List<Map<String, Object>> candidates = hotArticles.stream()
                        .filter(a -> a.getArticleId() != articleId)
                        .limit(15)
                        .map(a -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("id", String.valueOf(a.getArticleId()));
                            m.put("title", a.getArticleTitle());
                            return m;
                        })
                        .collect(Collectors.toList());
                req.setCandidates(candidates);
            }

            // 4. 调用 AI 推荐
            com.jd.wego.vo.ai.AiApiResponse<RecommendResponse> resp = aiGatewayService.callRecommend(req);
            if (resp.getData() != null) {
                return Result.success(resp.getData());
            } else {
                return Result.error(new CodeMsg(500, "推荐服务暂不可用"));
            }
        } catch (Exception e) {
            log.error("AI recommend error for articleId={}", articleId, e);
            return Result.error(new CodeMsg(500, "推荐服务异常"));
        }
    }

    /**
     * 查询熔断器状态
     */
    @GetMapping("/ai/circuit/status")
    @ResponseBody
    public Result<java.util.Map<String, Object>> aiCircuitStatus() {
        java.util.Map<String, Object> status = new HashMap<>();
        status.put("consecutiveFailures", aiFacadeService.getConsecutiveFailures());
        status.put("circuitBreakerOpen", aiFacadeService.getConsecutiveFailures() >= 5);
        return Result.success(status);
    }

    /**
     * 重置熔断器
     */
    @PostMapping("/ai/circuit/reset")
    @ResponseBody
    public Result<Boolean> aiCircuitReset() {
        aiFacadeService.resetCircuitBreaker();
        return Result.success(true);
    }

}
