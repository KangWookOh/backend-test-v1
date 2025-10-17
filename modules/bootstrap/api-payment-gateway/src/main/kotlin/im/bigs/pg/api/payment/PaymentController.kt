package im.bigs.pg.api.payment

import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import im.bigs.pg.api.payment.dto.Summary
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.PaymentUseCase
import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryPaymentsUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * 결제 API 진입점.
 * - POST: 결제 생성
 * - GET: 결제 조회(커서 페이지네이션 + 통계)
 */
@RestController
@RequestMapping("/api/v1/payments")
@Validated
@Tag(name = "결제 API", description = "결제 생성 및 조회 API")
class PaymentController(
    private val paymentUseCase: PaymentUseCase,
    private val queryPaymentsUseCase: QueryPaymentsUseCase,
) {

    /** 결제 생성 요청 페이로드(간소화된 필드). */

    /** API 응답을 위한 변환용 DTO. 도메인 모델을 그대로 노출하지 않습니다. */

    /**
     * 결제 생성.
     *
     * @param req 결제 요청 본문
     * @return 생성된 결제 요약 응답
     */
    @PostMapping
    @Operation(
        summary = "결제 생성",
        description = """
            결제 승인을 요청하고 결과를 저장합니다.
            - 홀수 partnerId: MockPgClient 사용 (항상 성공)
            - 짝수 partnerId: TestPayPgClient 사용 (실제 API 연동)
            - 제휴사별 수수료 정책이 자동 적용됩니다.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "결제 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청", content = [Content()]),
            ApiResponse(responseCode = "500", description = "서버 오류", content = [Content()])
        ]
    )
    fun create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "결제 요청 정보",
            required = true
        )
        @RequestBody req: CreatePaymentRequest
    ): ResponseEntity<PaymentResponse> {
        val saved = paymentUseCase.pay(
            PaymentCommand(
                partnerId = req.partnerId,
                amount = req.amount,
                cardBin = req.cardBin,
                cardLast4 = req.cardLast4,
                productName = req.productName,
            ),
        )
        return ResponseEntity.ok(PaymentResponse.from(saved))
    }

    /** 목록 + 통계를 포함한 조회 응답. */

    /**
     * 결제 조회(커서 기반 페이지네이션 + 통계).
     *
     * @param partnerId 제휴사 필터
     * @param status 상태 필터
     * @param from 조회 시작 시각(ISO-8601)
     * @param to 조회 종료 시각(ISO-8601)
     * @param cursor 다음 페이지 커서
     * @param limit 페이지 크기(기본 20)
     * @return 목록/통계/커서 정보
     */
    @GetMapping
    @Operation(
        summary = "결제 내역 조회",
        description = """
            결제 내역을 조회하고 통계 정보를 제공합니다.
            - 커서 기반 페이지네이션 (createdAt desc, id desc)
            - summary는 필터와 동일한 집합을 대상으로 집계
            - 모든 파라미터는 선택사항
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청", content = [Content()])
        ]
    )
    fun query(
        @Parameter(description = "제휴사 ID") @RequestParam(required = false) partnerId: Long?,
        @Parameter(description = "결제 상태 (APPROVED 등)") @RequestParam(required = false) status: String?,
        @Parameter(description = "조회 시작 시각 (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime?,
        @Parameter(description = "조회 종료 시각 (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime?,
        @Parameter(description = "다음 페이지 커서 (Base64 인코딩)") @RequestParam(required = false) cursor: String?,
        @Parameter(description = "페이지 크기 (기본 20)") @RequestParam(defaultValue = "20") limit: Int,
    ): ResponseEntity<QueryResponse> {
        val res = queryPaymentsUseCase.query(
            QueryFilter(partnerId, status, from, to, cursor, limit),
        )
        return ResponseEntity.ok(
            QueryResponse(
                items = res.items.map { PaymentResponse.from(it) },
                summary = Summary(res.summary.count, res.summary.totalAmount, res.summary.totalNetAmount),
                nextCursor = res.nextCursor,
                hasNext = res.hasNext,
            ),
        )
    }
}
