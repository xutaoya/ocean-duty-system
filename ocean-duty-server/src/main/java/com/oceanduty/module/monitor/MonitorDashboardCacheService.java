package com.oceanduty.module.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanduty.module.monitor.domain.DashboardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 监控仪表盘 Redis 缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorDashboardCacheService {

    private static final String CACHE_KEY = "ocean-duty:monitor:dashboard";

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private RedisTemplate<String, String> monitorRedisTemplate;

    @Value("${ocean-duty.cache.enabled:false}")
    private boolean enabled;

    @Value("${ocean-duty.cache.dashboard-ttl-seconds:60}")
    private long dashboardTtlSeconds;

    /**
     * 读取缓存的仪表盘数据
     */
    public Optional<DashboardVO> get() {
        if (!isAvailable()) {
            return Optional.empty();
        }
        try {
            String json = monitorRedisTemplate.opsForValue().get(CACHE_KEY);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, DashboardVO.class));
        } catch (Exception e) {
            log.warn("读取仪表盘缓存失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 写入仪表盘缓存
     */
    public void put(DashboardVO dashboard) {
        if (!isAvailable() || dashboard == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(dashboard);
            monitorRedisTemplate.opsForValue().set(CACHE_KEY, json, dashboardTtlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("写入仪表盘缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清除仪表盘缓存
     */
    public void evict() {
        if (!isAvailable()) {
            return;
        }
        try {
            monitorRedisTemplate.delete(CACHE_KEY);
        } catch (Exception e) {
            log.warn("清除仪表盘缓存失败: {}", e.getMessage());
        }
    }

    private boolean isAvailable() {
        return enabled && monitorRedisTemplate != null && dashboardTtlSeconds > 0;
    }
}
