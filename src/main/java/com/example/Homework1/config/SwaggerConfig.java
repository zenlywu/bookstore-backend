package com.example.Homework1.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
@SecurityScheme(
    name = "bearerAuth", 
    type = SecuritySchemeType.HTTP, 
    scheme = "bearer", 
    bearerFormat = "JWT"
)
@SecurityRequirement(name = "bearerAuth")  // 🔥 確保 `Swagger` 所有 API 需要 `Authorization Header`
public class SwaggerConfig {

    public SwaggerConfig() {
        System.out.println("✅ SwaggerConfig 已加載！");
    }

    @Bean
    public GroupedOpenApi publicApi() {
        System.out.println("✅ Swagger API 設定初始化！");
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }
}

