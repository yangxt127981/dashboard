package com.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Service
public class IoaService {

    private static final Logger log = LoggerFactory.getLogger(IoaService.class);

    @Value("${ioa.host}")
    private String host;

    @Value("${ioa.appid}")
    private String appId;

    @Value("${ioa.secret-key}")
    private String secretKey;

    @Value("${ioa.secret-id}")
    private String secretId;

    @Value("${ioa.api-version}")
    private String apiVersion;

    private final RestTemplate restTemplate = buildTrustAllRestTemplate();

    /**
     * 调用 IOA 验签接口校验 Ticket
     *
     * @param userId 工号（IOA UserID）
     * @param ticket 从本机 IOA 客户端获取的 Ticket
     * @return true 表示验证通过
     */
    public boolean checkTicket(String userId, String ticket) {
        String url = host + "/OpenApi/V1/ClientLogin/SSO/CheckTicket";
        log.info("IOA checkTicket 请求: url={}, userId={}", url, userId);

        Map<String, String> body = new HashMap<>();
        body.put("UserID", userId);
        body.put("Ticket", ticket);
        body.put("AppID", appId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Secret-Id", secretId);
        headers.set("X-Secret-Key", secretKey);
        headers.set("X-Api-Version", apiVersion);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<?, ?>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, new org.springframework.core.ParameterizedTypeReference<Map<?, ?>>() {});
            Map<?, ?> result = response.getBody();
            log.info("IOA checkTicket 响应: {}", result);
            if (result == null) return false;
            // 响应结构: {Code=0, Message=Success, Data={VerifyResult=true}}
            Object data = result.get("Data");
            if (!(data instanceof Map<?, ?> dataMap)) return false;
            Object verifyResult = dataMap.get("VerifyResult");
            // 兼容 boolean true 和 integer 1 两种返回格式
            return Boolean.TRUE.equals(verifyResult) || Integer.valueOf(1).equals(verifyResult);
        } catch (Exception e) {
            log.error("IOA checkTicket 调用异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建跳过 SSL 证书校验的 RestTemplate（适用于内网自签名证书）
     */
    private static RestTemplate buildTrustAllRestTemplate() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            return new RestTemplate(new SimpleClientHttpRequestFactory());
        } catch (Exception e) {
            LoggerFactory.getLogger(IoaService.class).warn("构建 TrustAll RestTemplate 失败，使用默认配置", e);
            return new RestTemplate();
        }
    }
}
