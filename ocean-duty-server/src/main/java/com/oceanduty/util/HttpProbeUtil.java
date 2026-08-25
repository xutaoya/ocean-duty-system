package com.oceanduty.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP 网站探测工具
 */
public final class HttpProbeUtil {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private HttpProbeUtil() {
    }

    /**
     * 探测网站访问状态
     *
     * @param url 网站地址
     * @return 探测结果 [httpStatus, responseTimeMs, errorMessage]
     */
    public static ProbeResult probe(String url) {
        return probe(url, 10000);
    }

    /**
     * 探测网站访问状态
     *
     * @param url       网站地址
     * @param timeoutMs 超时时间(ms)
     * @return 探测结果
     */
    public static ProbeResult probe(String url, int timeoutMs) {
        int timeout = Math.max(timeoutMs, 1000);
        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeout))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .GET()
                    .build();
            HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            long cost = System.currentTimeMillis() - start;
            return new ProbeResult(response.statusCode(), (int) cost, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            return new ProbeResult(null, (int) cost, e.getMessage());
        }
    }

    /**
     * 探测结果
     */
    public record ProbeResult(Integer httpStatus, Integer responseTime, String errorMessage) {
    }
}
