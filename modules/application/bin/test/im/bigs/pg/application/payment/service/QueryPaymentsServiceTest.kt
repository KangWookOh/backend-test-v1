package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueryPaymentsServiceTest {
    private val paymentRepository = mockk<PaymentOutPort>()
    private val service = QueryPaymentsService(paymentRepository)

    @Test
    @DisplayName("결제 내역 조회 시 빈 결과를 반환해야 한다")
    fun `결제 내역 조회 시 빈 결과를 반환해야 한다`() {
        // Given
        val filter = QueryFilter(limit = 20)
        val emptyPage = PaymentPage(
            items = emptyList(),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )
        val emptySummary = PaymentSummaryProjection(0, BigDecimal.ZERO, BigDecimal.ZERO)

        every { paymentRepository.findBy(any<PaymentQuery>()) } returns emptyPage
        every { paymentRepository.summary(any<PaymentSummaryFilter>()) } returns emptySummary

        // When
        val result = service.query(filter)

        // Then
        assertEquals(0, result.items.size)
        assertEquals(0, result.summary.count)
        assertEquals(BigDecimal.ZERO, result.summary.totalAmount)
        assertEquals(BigDecimal.ZERO, result.summary.totalNetAmount)
        assertNull(result.nextCursor)
        assertFalse(result.hasNext)
    }

    @Test
    @DisplayName("결제 내역 조회 시 통계와 함께 결과를 반환해야 한다")
    fun `결제 내역 조회 시 통계와 함께 결과를 반환해야 한다`() {
        // Given
        val filter = QueryFilter(partnerId = 1L, status = "APPROVED", limit = 20)
        val payment = Payment(
            id = 1L,
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardLast4 = "4242",
            approvalCode = "APPROVAL-123",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED,
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 1, 0, 0)
        )

        val paymentPage = PaymentPage(
            items = listOf(payment),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )
        val summary = PaymentSummaryProjection(1, BigDecimal("10000"), BigDecimal("9600"))

        every { paymentRepository.findBy(any<PaymentQuery>()) } returns paymentPage
        every { paymentRepository.summary(any<PaymentSummaryFilter>()) } returns summary

        // When
        val result = service.query(filter)

        // Then
        assertEquals(1, result.items.size)
        assertEquals(1L, result.summary.count)
        assertEquals(BigDecimal("10000"), result.summary.totalAmount)
        assertEquals(BigDecimal("9600"), result.summary.totalNetAmount)
        assertNull(result.nextCursor)
        assertFalse(result.hasNext)
    }

    @Test
    @DisplayName("다음 페이지가 있을 때 커서를 반환해야 한다")
    fun `다음 페이지가 있을 때 커서를 반환해야 한다`() {
        // Given
        val filter = QueryFilter(limit = 1)
        val payment = Payment(
            id = 1L,
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardLast4 = "4242",
            approvalCode = "APPROVAL-123",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED,
            createdAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            updatedAt = LocalDateTime.of(2024, 1, 1, 0, 0)
        )

        val nextCursorTime = LocalDateTime.of(2024, 1, 1, 0, 0)
        val paymentPage = PaymentPage(
            items = listOf(payment),
            hasNext = true,
            nextCursorCreatedAt = nextCursorTime,
            nextCursorId = 1L
        )
        val summary = PaymentSummaryProjection(1, BigDecimal("10000"), BigDecimal("9600"))

        every { paymentRepository.findBy(any<PaymentQuery>()) } returns paymentPage
        every { paymentRepository.summary(any<PaymentSummaryFilter>()) } returns summary

        // When
        val result = service.query(filter)

        // Then
        assertEquals(1, result.items.size)
        assertTrue(result.hasNext)
        assertTrue(result.nextCursor != null)
    }

    @Test
    @DisplayName("잘못된 상태값은 null로 처리되어야 한다")
    fun `잘못된 상태값은 null로 처리되어야 한다`() {
        // Given
        val filter = QueryFilter(status = "INVALID_STATUS", limit = 20)
        val emptyPage = PaymentPage(
            items = emptyList(),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )
        val emptySummary = PaymentSummaryProjection(0, BigDecimal.ZERO, BigDecimal.ZERO)

        every { paymentRepository.findBy(any<PaymentQuery>()) } returns emptyPage
        every { paymentRepository.summary(any<PaymentSummaryFilter>()) } returns emptySummary

        // When
        val result = service.query(filter)

        // Then
        assertEquals(0, result.items.size)
        assertNull(result.nextCursor)
        assertFalse(result.hasNext)
    }
}
