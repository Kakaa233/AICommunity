package com.jd.wego.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.wego.vo.ai.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * AI 服务 HTTP 网关 —— 负责调用 Python AI 服务的 API
 */
@Service
public class AiGatewayService {

    private static final Logger log = LoggerFactory.getLogger(AiGatewayService.class);

    @Value("${ai.service.base-url}")
    private String baseUrl;

    @Value("${ai.service.service-key}")
    private String serviceKey;

    @Value("${ai.service.connect-timeout}")
    private int connectTimeout;

    @Value("${ai.service.socket-timeout}")
    private int socketTimeout;

    private CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(socketTimeout)
                .build();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
        log.info("AiGatewayService initialized, baseUrl={}, connectTimeout={}, socketTimeout={}",
                baseUrl, connectTimeout, socketTimeout);
    }

    // ==================== HMAC-SHA256 签名 ====================

    private String computeSignature(long timestamp, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] raw = mac.doFinal(String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(raw);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 computation failed", e);
        }
    }

    // ==================== 通用调用方法 ====================

    /**
     * 调用 AI 服务的通用方法
     *
     * @param endpoint  API 路径，如 /api/v1/polish
     * @param request   请求体对象
     * @param valueType 响应 data 字段的类型引用（用于泛型反序列化）
     * @param <TReq>    请求类型
     * @param <TResp>   响应 data 类型
     * @return AiApiResponse 包装的响应
     */
    public <TReq, TResp> AiApiResponse<TResp> callAiService(
            String endpoint, TReq request, Class<TResp> valueType) {

        long start = System.currentTimeMillis();
        String url = baseUrl + endpoint;

        try {
            // 1. 构建请求体
            String jsonBody = objectMapper.writeValueAsString(request);

            // 2. 计算签名
            long timestamp = System.currentTimeMillis() / 1000;
            String signature = computeSignature(timestamp, serviceKey);

            // 3. 创建 HTTP POST 请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setHeader("X-Service-Key", serviceKey);
            httpPost.setHeader("X-Timestamp", String.valueOf(timestamp));
            httpPost.setHeader("X-Signature", signature);
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            // 4. 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                log.info("AI service call {} | status={} | body={}", endpoint, statusCode,
                        responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody);

                if (statusCode != 200) {
                    log.warn("AI service returned non-200 status: {} for endpoint: {}", statusCode, endpoint);
                    return buildFallbackResponse(valueType, "AI service error: HTTP " + statusCode);
                }

                // 5. 解析响应
                // 先用原始类型解析外层
                AiApiResponse<Object> rawResp = objectMapper.readValue(responseBody,
                        new TypeReference<AiApiResponse<Object>>() {});

                if (rawResp.getCode() != 0) {
                    log.warn("AI service business error: code={}, message={} for endpoint: {}",
                            rawResp.getCode(), rawResp.getMessage(), endpoint);
                    return buildFallbackResponse(valueType, rawResp.getMessage());
                }

                // 6. 转换 data 字段
                AiApiResponse<TResp> result = new AiApiResponse<>();
                result.setCode(rawResp.getCode());
                result.setMessage(rawResp.getMessage());
                result.setLatencyMs(rawResp.getLatencyMs());
                result.setFallbackHit(rawResp.isFallbackHit());
                result.setModel(rawResp.getModel());

                if (rawResp.getData() != null) {
                    // 将 data 从 LinkedHashMap 转为目标类型
                    Object rawData = rawResp.getData();
                    if (rawData instanceof java.util.LinkedHashMap || rawData instanceof java.util.Map) {
                        String dataJson = objectMapper.writeValueAsString(rawData);
                        TResp data = objectMapper.readValue(dataJson, valueType);
                        result.setData(data);
                    } else {
                        // data 已经是目标类型（如 String）
                        result.setData((TResp) rawData);
                    }
                }

                long elapsed = System.currentTimeMillis() - start;
                log.info("AI service call {} completed in {}ms, fallback={}", endpoint, elapsed, result.isFallbackHit());
                return result;

            }
        } catch (Exception e) {
            log.error("AI service call failed for endpoint: {} | error: {}", endpoint, e.getMessage(), e);
            return buildFallbackResponse(valueType, "Gateway error: " + e.getMessage());
        }
    }

    /**
     * 当 AI 服务调用失败时，返回 fallback 空响应
     */
    private <TResp> AiApiResponse<TResp> buildFallbackResponse(Class<TResp> valueType, String message) {
        AiApiResponse<TResp> fallback = new AiApiResponse<>();
        fallback.setCode(5002);
        fallback.setMessage(message);
        fallback.setData(null);
        fallback.setLatencyMs(0);
        fallback.setFallbackHit(true);
        fallback.setModel("fallback");
        return fallback;
    }

    // ==================== 7 个业务方法 ====================

    public AiApiResponse<PolishResponse> callPolish(PolishRequest request) {
        return callAiService("/api/v1/polish", request, PolishResponse.class);
    }

    public AiApiResponse<DraftResponse> callDraft(DraftRequest request) {
        return callAiService("/api/v1/draft", request, DraftResponse.class);
    }

    public AiApiResponse<SummaryResponse> callSummary(SummaryRequest request) {
        return callAiService("/api/v1/summary", request, SummaryResponse.class);
    }

    public AiApiResponse<QualityResponse> callQuality(QualityRequest request) {
        return callAiService("/api/v1/quality", request, QualityResponse.class);
    }

    public AiApiResponse<ModerationResponse> callModeration(ModerationRequest request) {
        return callAiService("/api/v1/moderation", request, ModerationResponse.class);
    }

    public AiApiResponse<TopicsResponse> callTopics(TopicsRequest request) {
        return callAiService("/api/v1/topics", request, TopicsResponse.class);
    }

    public AiApiResponse<RecommendResponse> callRecommend(RecommendRequest request) {
        return callAiService("/api/v1/recommend", request, RecommendResponse.class);
    }
}
