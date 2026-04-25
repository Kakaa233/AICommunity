## Plan: AI辅助模块独立服务落地

在仓库根目录新增独立 AI 服务（FastAPI），由现有 Java 后端作为业务网关转发与编排。优先采用同步 HTTP + 超时降级，先接第三方模型 API；将“在线强交互能力”（润色、问答草稿、长文摘要、质量评分）做同步返回，将“治理和计算型能力”（审核、专题聚合、推荐）做异步任务，避免拖慢发帖主链路。

**Steps**
1. Phase 0 - 架构与边界冻结（*阻塞后续所有步骤*）
2. 明确 AI 服务目录与运行边界：与现有 `src`、`web` 同级新增 `ai-service`（独立进程、独立端口、独立配置）。
3. 冻结模块边界：现有 Java 后端继续负责登录态、权限、数据持久化与对前端契约；AI 服务只负责推理与评分，不直接改业务库。
4. 冻结接口契约 V1：统一请求上下文（traceId、userId、scene、contentType），统一响应结构（code/message/data/latencyMs/fallbackHit/model）。
5. 定义服务级 SLA：同步接口超时 1.5s~3s（按能力分级），失败降级（返回空增强、不阻塞发布）。

6. Phase 1 - 基础设施与目录搭建（*depends on 1*）
7. 创建 AI 服务基础骨架：FastAPI + Pydantic + Uvicorn + httpx + tenacity + structlog（或 loguru）+ pytest。
8. 建立配置体系：`dev/test/prod` 环境变量、模型 Key、超时、重试、限流、熔断阈值。
9. 增加服务鉴权：Java -> AI 服务使用 `X-Service-Key` + 时间戳签名（HMAC），并校验重放窗口。
10. 增加可观测性：请求日志、错误日志、调用耗时、模型失败率、降级率指标。

11. Phase 2 - Java 网关接入层（*depends on 6*）
12. 在现有后端新增 `AiGatewayService`（HTTP Client 封装）与 `AiFacadeService`（业务编排）。
13. 将发帖与编辑流程接入 AI 编排点：发布前可选润色；发布后触发摘要/评分/审核；详情页读取推荐与专题。
14. 统一错误处理：AI 异常映射为业务可消费错误码（超时、服务不可用、输入无效），并提供用户友好提示。
15. 增加熔断与降级：当 AI 服务失败率连续超阈值时，自动走降级路径（仅存原文、跳过 AI 字段）。

16. Phase 3 - 数据模型与任务流（*depends on 11, parallel with 17*）
17. 扩展内容表/任务表：
18. 内容 AI 字段：`ai_summary`、`ai_keypoints_json`、`ai_quality_score`、`ai_quality_dims_json`、`ai_review_status`、`ai_review_reason`、`ai_tags_json`。
19. 任务表（建议）：`ai_task`（taskType、bizId、status、retryCount、payload、result、nextRunAt）。
20. 推荐/专题表（建议）：`ai_recommendation`、`ai_topic`、`ai_topic_item`。
21. 任务执行策略：审核/专题聚合/推荐走异步（Quartz 或独立 worker），支持重试与死信。

22. Phase 4 - AI 能力模块实现（*depends on 11 and 16*）
23. 模块 A（同步）内容润色：输入标题+正文，输出润色标题、摘要、3-5标签。
24. 模块 B（同步）问答草稿：输入问题标题+描述，输出 <=500 字结构化草稿。
25. 模块 C（同步）长文摘要与要点：输入全文，输出 <=200 字摘要 + 3-7 要点。
26. 模块 D（同步）质量评分：输出总分与三维分（完整性/清晰度/价值度）。
27. 模块 E（异步）审核助手：输出通过/标记/驳回、违规类型、置信度，并落库待人工复核。
28. 模块 F（异步）专题聚合：定时聚类生成专题页数据。
29. 模块 G（异步+缓存）相关推荐：详情页请求时优先读缓存，失效后异步刷新。

30. Phase 5 - 前端接入（*depends on 22, parallel with 31*）
31. 写作页新增“AI润色”按钮与结果确认区（标题/摘要/标签可编辑后回填）。
32. 问答帖发布后展示“AI参考草稿”（可一键采纳/继续编辑）。
33. 详情页新增“摘要/要点卡片”“质量评分展示”“相关推荐”。
34. 管理端（或临时后台页）增加“AI审核队列”人工复核入口。

35. Phase 6 - 安全与合规（*depends on 6 and 11*）
36. 数据最小化：传模型前做脱敏（手机号、学号、邮箱、身份证样式等）。
37. 审计日志：记录谁在何时触发了何种 AI 能力、使用了何模型版本。
38. 风险兜底：模型拒答、违规误判、超时失败都不得阻塞原始内容保存与人工流程。

39. Phase 7 - 测试、灰度与上线（*depends on 22 and 30*）
40. 单元测试：AI 服务输入校验、提示词模板、响应解析、异常重试。
41. 集成测试：Java -> AI 服务全链路、超时/降级/熔断场景、任务重试。
42. 验收测试：按你给出的 3.1~3.8 逐条设计 UAT 用例与通过标准。
43. 灰度策略：先对内部账号开放 AI 功能开关，再扩大到全站。
44. 运营指标：生成成功率、平均时延、人工复核通过率、推荐点击率、用户采纳率。

**Relevant files**
- `f:/Projects/Community_2/wego/pom.xml` — 现有后端依赖与构建基线；用于新增 HTTP 客户端与容错依赖评估。
- `f:/Projects/Community_2/wego/src/main/resources/application.properties` — 新增 AI 服务地址、鉴权密钥、超时、开关配置。
- `f:/Projects/Community_2/wego/src/main/java/com/jd/wego/controller/ArticleController.java` — 发帖/编辑链路接入 AI 编排入口。
- `f:/Projects/Community_2/wego/src/main/java/com/jd/wego/controller/IndexController.java` — 详情页数据聚合点，接入摘要/推荐读取。
- `f:/Projects/Community_2/wego/src/main/java/com/jd/wego/controller/CommentController.java` — 评论与内容治理联动参考点（审核策略复用）。
- `f:/Projects/Community_2/wego/src/main/java/com/jd/wego/async/EventProducer.java` — 现有异步风格参考，可用于 AI 异步任务触发。
- `f:/Projects/Community_2/wego/src/main/java/com/jd/wego/async/EventConsumer.java` — 异步消费机制参考，可评估复用或旁路。
- `f:/Projects/Community_2/wego/web/src/views/writeArticle.vue` — AI 润色入口与回填交互主改造点。
- `f:/Projects/Community_2/wego/web/src/components/detail/content.vue` — 摘要/评分/推荐展示主改造点。
- `f:/Projects/Community_2/wego/web/src/router/index.js` — 新增审核页/专题页路由（若一期纳入可视化后台）。

**Verification**
1. 合同测试：对 AI 服务 7 个能力接口做 request/response schema 校验（必填、长度、错误码）。
2. 集成测试：在 Java 侧模拟 AI 服务超时、5xx、空响应，验证降级不阻塞发布。
3. 数据一致性测试：发帖后检查内容主表 AI 字段、任务表状态、审核队列是否一致。
4. 前端联调测试：写作页 AI 润色回填、详情页摘要与推荐展示、审核提示链路。
5. 回归测试：登录/发布/编辑/评论/搜索原功能不回退。
6. 性能测试：同步 AI 接口 P95 时延、失败率、熔断触发与恢复行为。

**Decisions**
- 技术栈：独立 AI 服务采用 Python FastAPI。
- 调用方式：以同步 HTTP + 超时降级为主，治理类能力采用异步任务。
- 模型策略：先接第三方 API，保留后续本地模型切换能力。
- 一期范围：你确认了润色、问答草稿、长文摘要、质量评分、审核、专题聚合、推荐全部纳入。
- 约束边界：不做付费、广告、校外开放、深度论文查重、跨校互通、实时音视频。

**Further Considerations**
1. 一期全模块工作量较大，建议切为 P0（润色/摘要/问答/评分/审核）+ P1（专题聚合/推荐），保持 2-4 周可交付。
2. 现有 SpringBoot 2.3 + Java8 较老，建议 AI 网关依赖尽量轻量，避免引入高版本不兼容。
3. 建议在仓库根目录新增统一 `docs/ai-service/`（接口契约、错误码、提示词版本、验收用例）作为跨端单一事实来源。