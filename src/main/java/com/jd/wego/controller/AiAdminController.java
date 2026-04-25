package com.jd.wego.controller;

import com.jd.wego.entity.Article;
import com.jd.wego.service.ArticleService;
import com.jd.wego.utils.CodeMsg;
import com.jd.wego.utils.Result;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * AI 审核管理后台 Controller
 *
 * @author AI
 */
@Controller
@RequestMapping("/admin/ai")
public class AiAdminController {

    private static final Logger log = LoggerFactory.getLogger(AiAdminController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * 获取待审核文章列表（status = 'flag' 或 'pending'）
     * GET /admin/ai/review/list
     */
    @GetMapping("/review/list")
    @ResponseBody
    public Result<List<Article>> getReviewList() {
        try {
            List<String> statuses = Arrays.asList("flag", "pending");
            List<Article> list = articleService.selectByReviewStatus(statuses);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取审核列表失败", e);
            return Result.error(new CodeMsg(500, "获取审核列表失败"));
        }
    }

    /**
     * 审核通过文章
     * POST /admin/ai/review/approve
     */
    @PostMapping("/review/approve")
    @ResponseBody
    public Result<Boolean> approveArticle(@RequestParam int articleId,
                                           @RequestParam(required = false) String reason) {
        try {
            Article article = articleService.selectArticleByArticleId(articleId);
            if (article == null) {
                return Result.error(new CodeMsg(400, "文章不存在"));
            }
            article.setAiReviewStatus("pass");
            article.setAiReviewReason(reason);
            article.setUpdateTime(new Date());
            articleService.updateArticle(article);
            log.info("AI审核通过 articleId={}", articleId);
            return Result.success(true);
        } catch (Exception e) {
            log.error("审核通过失败 articleId={}", articleId, e);
            return Result.error(new CodeMsg(500, "审核操作失败"));
        }
    }

    /**
     * 驳回文章
     * POST /admin/ai/review/reject
     */
    @PostMapping("/review/reject")
    @ResponseBody
    public Result<Boolean> rejectArticle(@RequestParam int articleId,
                                          @RequestParam String reason) {
        try {
            Article article = articleService.selectArticleByArticleId(articleId);
            if (article == null) {
                return Result.error(new CodeMsg(400, "文章不存在"));
            }
            article.setAiReviewStatus("rejected");
            article.setAiReviewReason(reason);
            article.setUpdateTime(new Date());
            articleService.updateArticle(article);
            log.info("AI审核驳回 articleId={}, reason={}", articleId, reason);
            return Result.success(true);
        } catch (Exception e) {
            log.error("审核驳回失败 articleId={}", articleId, e);
            return Result.error(new CodeMsg(500, "审核操作失败"));
        }
    }
}
