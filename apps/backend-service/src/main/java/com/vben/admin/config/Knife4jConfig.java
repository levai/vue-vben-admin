package com.vben.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger) 配置
 * Knife4j 4.4.0 基于 SpringDoc OpenAPI 3，支持 Spring Boot 3
 *
 * @author vben
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vben Admin API 文档")
                        .description("Vben Admin 后端服务 API 接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vben Admin")
                                .email("support@vben.com")));
    }
}
