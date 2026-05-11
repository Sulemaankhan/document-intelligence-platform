package com.dip.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentIntelligenceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PDF RAG API")
                        .version("0.1.0")
                        .description("Ingest PDFs (chunk + embed) and ask questions with POST /api/rag/query. "
                                + "Answers are grounded in retrieved passages only.")

                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
