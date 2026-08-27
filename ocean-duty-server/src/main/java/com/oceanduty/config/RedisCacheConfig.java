package com.oceanduty.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Redis 缓存配置（仅在开启缓存时生效）
 */
@Configuration
@ConditionalOnProperty(prefix = "ocean-duty.cache", name = "enabled", havingValue = "true")
public class RedisCacheConfig {

    @Bean
    public RedisConnectionFactory monitorRedisConnectionFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) {
            configuration.setPassword(password);
        }
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, String> monitorRedisTemplate(RedisConnectionFactory monitorRedisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(monitorRedisConnectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.string());
        template.afterPropertiesSet();
        return template;
    }
}
