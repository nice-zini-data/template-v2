package com.zinidata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI 3.x 설정
 * 
 * <p>Swagger UI 및 API 문서 자동 생성 설정</p>
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 기본 정보 설정
     * 
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ZiniData API Documentation")
                .description("ZiniData 상권분석 플랫폼 REST API 문서")
                .version("1.0.0")
                .contact(new Contact()
                    .name("NICE ZiniData 개발팀")
                    .url("https://m.nicebizmap.co.kr")
                    .email("dev@zinidata.com"))
                .license(new License()
                    .name("NICE ZiniData License")
                    .url("https://m.nicebizmap.co.kr/license")))
                .addServersItem(new Server()
                    .url("http://localhost:8001")
                    .description("로컬 개발 서버"))
                .addServersItem(new Server()
                    .url("https://ncs.nicebizmap.co.kr")
                    .description("운영 서버"))
                    .addServersItem(new Server()
                        .url("https://devncs.nicebizmap.co.kr")
                        .description("스테이징 서버"))
                    .addServersItem(new Server()
                        .url("https://devncs2.nicebizmap.co.kr")
                        .description("개발 서버"));
    }
} 