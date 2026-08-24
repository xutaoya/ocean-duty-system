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
        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
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
