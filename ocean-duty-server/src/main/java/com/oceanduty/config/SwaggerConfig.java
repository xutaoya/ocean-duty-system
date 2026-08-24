package com.oceanduty.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 接口文档配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("海洋预报发布值班监控系统 API")
                        .description("网站监控、模块监控、值班日志管理接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("ocean-duty")));
    }
}
