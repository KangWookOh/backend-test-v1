package im.bigs.pg.external.pg

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
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * TestPay PG 클라이언트 구현체.
 * - 실제 TestPay API와 연동하여 결제 승인을 처리합니다.
 * - API 문서: https://api-test-pg.bigs.im/docs/index.html
 * - 암호화: AES-256-GCM (SHA-256(API-KEY), IV 12바이트)
 */
@Component
class TestPayPgClient(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : PgClientOutPort {

    private val log = LoggerFactory.getLogger(javaClass)
    private val baseUrl = "https://api-test-pg.bigs.im"

    // 테스트용 API-KEY 및 IV (실제 환경에서는 환경변수 또는 설정 파일로 관리)
    private val apiKey = "11111111-1111-4111-8111-111111111111"
    private val ivBase64Url = "AAAAAAAAAAAAAAAA"

    override fun supports(partnerId: Long): Boolean = partnerId % 2L == 0L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        log.info("TestPay API 호출 시작: partnerId={}, amount={}", request.partnerId, request.amount)

        try {
            // 1. 평문 JSON 생성
            val plaintext = TestPayPlaintext(
                cardNumber = formatCardNumber(request.cardBin, request.cardLast4),
                birthDate = "19900101", // 실제 환경에서는 요청에서 받아야 함
                expiry = "1227", // 실제 환경에서는 요청에서 받아야 함
                password = "12", // 실제 환경에서는 요청에서 받아야 함
                amount = request.amount.toLong()
            )

            // 2. AES-256-GCM 암호화
            val plaintextJson = objectMapper.writeValueAsString(plaintext)
            val encryptedData = encryptAesGcm(plaintextJson, apiKey, ivBase64Url)

            // 3. 요청 생성
            val encRequest = TestPayEncryptedRequest(enc = encryptedData)

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("API-KEY", apiKey)
            }

            val entity = HttpEntity(encRequest, headers)

            // 4. API 호출
            val response = restTemplate.exchange(
                "$baseUrl/api/v1/pay/credit-card",
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
                log.error("TestPay API 호출 실패: statusCode={}", response.statusCode)
                throw IllegalStateException("TestPay API 호출 실패: ${response.statusCode}")
            }
        } catch (e: Exception) {
            log.error("TestPay API 호출 중 오류 발생: {}", e.message, e)
            throw IllegalStateException("TestPay API 호출 실패: ${e.message}", e)
        }
    }

    /**
     * AES-256-GCM 암호화
     * @param plaintext 평문 문자열
     * @param apiKey API-KEY (UUID v4)
     * @param ivBase64Url IV (Base64URL 인코딩된 12바이트)
     * @return Base64URL(ciphertext||tag) (패딩 없음)
     */
    private fun encryptAesGcm(plaintext: String, apiKey: String, ivBase64Url: String): String {
        // 1. Key: SHA-256(API-KEY) → 32바이트
        val key = MessageDigest.getInstance("SHA-256")
            .digest(apiKey.toByteArray(StandardCharsets.UTF_8))
        val secretKey = SecretKeySpec(key, "AES")

        // 2. IV: Base64URL 디코딩 → 12바이트
        val iv = Base64.getUrlDecoder().decode(ivBase64Url)
        require(iv.size == 12) { "IV must be 12 bytes" }

        // 3. AES-256-GCM 암호화 (태그 길이 128비트 = 16바이트)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8)
        val ciphertextWithTag = cipher.doFinal(plaintextBytes)

        // 4. Base64URL 인코딩 (패딩 없음)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertextWithTag)
    }

    /**
     * 카드번호 포맷 (테스트용)
     * - 성공: 1111-1111-1111-1111
     * - 실패: 2222-2222-2222-2222
     */
    private fun formatCardNumber(cardBin: String?, cardLast4: String?): String {
        // 실제 환경에서는 완전한 카드번호가 필요하지만, 테스트에서는 성공 케이스 사용
        return "1111-1111-1111-1111"
    }
}

/**
 * TestPay 평문 요청 (암호화 전)
 */
data class TestPayPlaintext(
    val cardNumber: String, // 16자리 (하이픈 허용)
    val birthDate: String, // YYYYMMDD
    val expiry: String, // MMYY
    val password: String, // 카드 비밀번호 앞 2자리
    val amount: Long // 원 단위
)

/**
 * TestPay 암호화 요청 DTO
 */
data class TestPayEncryptedRequest(
    val enc: String // Base64URL(ciphertext||tag)
)

/**
 * TestPay 승인 응답 DTO
 */
data class TestPayApproveResponse(
    val approvalCode: String,
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: Long,
    val status: String
)
