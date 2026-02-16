package dev.lepelaka.kiosk.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kiosk API")
                        .description("키오스크 서비스 API 명세서 (DDD + MSA 지향)")
                        .version("v1.0.0"));
    }
}