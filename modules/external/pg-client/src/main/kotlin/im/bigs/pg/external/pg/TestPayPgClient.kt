package im.bigs.pg.external.pg

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.payment.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

/**
 * TestPay PG 클라이언트 구현체.
 * - 실제 TestPay API와 연동하여 결제 승인을 처리합니다.
 * - API 문서: https://api-test-pg.bigs.im/docs/index.html
 */
@Component
class TestPayPgClient(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : PgClientOutPort {

    private val log = LoggerFactory.getLogger(javaClass)
    private val baseUrl = "https://api-test-pg.bigs.im"

    override fun supports(partnerId: Long): Boolean = partnerId % 2L == 0L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        log.info("TestPay API 호출 시작: partnerId={}, amount={}", request.partnerId, request.amount)

        try {
            val approveRequest = TestPayApproveRequest(
                partnerId = request.partnerId,
                amount = request.amount,
                cardBin = request.cardBin,
                cardLast4 = request.cardLast4,
                productName = request.productName
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity(approveRequest, headers)
            val response: ResponseEntity<TestPayApproveResponse> = restTemplate.exchange(
                "$baseUrl/api/v1/payments/approve",
                HttpMethod.POST,
                entity,
                TestPayApproveResponse::class.java
            )

            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val responseBody = response.body!!
                log.info("TestPay API 호출 성공: approvalCode={}", responseBody.approvalCode)

                return PgApproveResult(
                    approvalCode = responseBody.approvalCode,
                    approvedAt = responseBody.approvedAt,
                    status = PaymentStatus.APPROVED
                )
            } else {
                throw IllegalStateException("TestPay API 호출 실패: ${response.statusCode}")
            }
        } catch (e: Exception) {
            log.error("TestPay API 호출 중 오류 발생", e)
            throw IllegalStateException("TestPay API 호출 실패: ${e.message}", e)
        }
    }
}

/**
 * TestPay 승인 요청 DTO
 */
data class TestPayApproveRequest(
    @JsonProperty("partner_id")
    val partnerId: Long,
    val amount: java.math.BigDecimal,
    @JsonProperty("card_bin")
    val cardBin: String?,
    @JsonProperty("card_last4")
    val cardLast4: String?,
    @JsonProperty("product_name")
    val productName: String?
)

/**
 * TestPay 승인 응답 DTO
 */
data class TestPayApproveResponse(
    @JsonProperty("approval_code")
    val approvalCode: String,
    @JsonProperty("approved_at")
    val approvedAt: LocalDateTime,
    val status: String
)
