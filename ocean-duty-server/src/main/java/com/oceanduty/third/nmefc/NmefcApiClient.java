package com.oceanduty.third.nmefc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 国家海洋预报中心网站 API 客户端
 */
@Slf4j
@Component
public class NmefcApiClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiBaseUrl;

    public NmefcApiClient(@Value("${ocean-duty.monitor.nmefc-api-base:https://www.nmefc.cn/api}") String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 查询灾害预警最新更新时间
     */
    public LocalDateTime fetchWarnHistoryLatest(String warnType, String imageFilter) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", warnType);
        params.put("year", String.valueOf(LocalDateTime.now().getYear()));
        JsonNode root = getJson("/data/warnHistoryList", params);
        JsonNode objNode = root.path("obj");
        if (!objNode.isObject()) {
            return null;
        }

        List<LocalDateTime> updateTimes = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
        while (fields.hasNext()) {
            JsonNode dayItems = fields.next().getValue();
            if (!dayItems.isArray()) {
                continue;
            }
            for (JsonNode item : dayItems) {
                if (matchImageFilter(item.path("image").asText(""), imageFilter)) {
                    parseDateTime(item.path("updateDate").asText(null)).ifPresent(updateTimes::add);
                }
            }
        }
        return updateTimes.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * 查询分析/综合预报最新更新时间
     */
    public LocalDateTime fetchAnalysisLatest(String type) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", type);
        JsonNode root = getJson("/data/analysisList", params);
        return extractLatestUpdateDate(root.path("obj"));
    }

    /**
     * 查询数值预报最新更新时间
     */
    public LocalDateTime fetchNumericalLatest(String element, String regionCode) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("element", element);
        params.put("regioncode", regionCode);
        JsonNode root = getJson("/data/numericalList", params);
        return extractLatestUpdateDate(root.path("obj"));
    }

    /**
     * 查询初始化数据更新时间
     */
    public LocalDateTime fetchInitLatest(String key) {
        JsonNode root = getJson("/data/init/" + key, Map.of());
        JsonNode updateDate = root.path("obj").path("updateDate");
        return parseDateTime(updateDate.asText(null)).orElse(null);
    }

    /**
     * 查询中尺度诊断最新更新时间
     */
    public LocalDateTime fetchDeepseaLatest(String region, String element) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("region", region);
        params.put("element", element);
        JsonNode root = getJson("/data/getDeepseaInfo", params);
        return parseDateTime(root.path("obj").path("updateDate").asText(null)).orElse(null);
    }

    /**
     * 查询极地预报最新更新时间
     */
    public LocalDateTime fetchPolarLatest(String region) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("region", region);
        JsonNode root = getJson("/data/getPolarRegionsList", params);
        return extractLatestUpdateDate(root.path("obj"));
    }

    private JsonNode getJson(String path, Map<String, String> params) {
        try {
            String url = apiBaseUrl + path;
            if (!params.isEmpty()) {
                url = url + "?" + buildQuery(params);
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Nmefc API 响应异常: {} status={}", url, response.statusCode());
                return OBJECT_MAPPER.createObjectNode();
            }
            return OBJECT_MAPPER.readTree(response.body());
        } catch (Exception e) {
            log.warn("Nmefc API 调用失败: {}{}", apiBaseUrl, path, e);
            return OBJECT_MAPPER.createObjectNode();
        }
    }

    private LocalDateTime extractLatestUpdateDate(JsonNode arrayNode) {
        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
            return null;
        }
        List<LocalDateTime> updateTimes = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            parseDateTime(item.path("updateDate").asText(null)).ifPresent(updateTimes::add);
        }
        return updateTimes.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    private boolean matchImageFilter(String imageUrl, String imageFilter) {
        if (imageFilter == null || imageFilter.isBlank()) {
            return true;
        }
        return imageUrl != null && imageUrl.contains(imageFilter);
    }

    private java.util.Optional<LocalDateTime> parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER));
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
