package im.bigs.pg.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI 문서화 설정.
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("나노바나나 페이먼츠 - 결제 도메인 API")
                    .version("1.0.0")
                    .description(
                        """
                        ## 결제 도메인 서버 API
                        
                        ### 주요 기능
                        - 결제 생성 (POST /api/v1/payments)
                        - 결제 조회 + 통계 (GET /api/v1/payments)
                        - 커서 기반 페이지네이션 지원
                        - 제휴사별 수수료 정책 적용
                        
                        ### PG 연동
                        - 홀수 partnerId: MockPgClient (로컬 테스트용)
                        - 짝수 partnerId: TestPayPgClient (실제 API 연동)
                        
                        ### 운영 지표
                        - Health Check: /actuator/health
                        - Metrics: /actuator/metrics
                        """.trimIndent()
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버")
                )
            )
    }
}
