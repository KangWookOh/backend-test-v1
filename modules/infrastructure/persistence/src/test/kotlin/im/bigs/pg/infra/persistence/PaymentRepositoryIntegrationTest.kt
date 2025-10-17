package im.bigs.pg.infra.persistence

import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.infra.persistence.config.JpaConfig
import im.bigs.pg.infra.persistence.payment.adapter.PaymentPersistenceAdapter
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@ContextConfiguration(classes = [JpaConfig::class])
@Import(PaymentPersistenceAdapter::class)
class PaymentRepositoryIntegrationTest {

    @Autowired
    private lateinit var paymentRepository: PaymentJpaRepository

    @Autowired
    private lateinit var paymentAdapter: PaymentPersistenceAdapter

    @Test
    @DisplayName("결제 저장 및 조회가 정상적으로 동작해야 한다")
    fun `결제 저장 및 조회가 정상적으로 동작해야 한다`() {
        // Given
        val payment = Payment(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardLast4 = "4242",
            approvalCode = "APPROVAL-123",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED
        )

        // When
        val saved = paymentAdapter.save(payment)

        // Then
        assertTrue(saved.id != null)
        assertEquals(1L, saved.partnerId)
        assertEquals(BigDecimal("10000"), saved.amount)
        assertEquals(BigDecimal("0.0300"), saved.appliedFeeRate)
        assertEquals(BigDecimal("400"), saved.feeAmount)
        assertEquals(BigDecimal("9600"), saved.netAmount)
        assertEquals("4242", saved.cardLast4)
        assertEquals("APPROVAL-123", saved.approvalCode)
        assertEquals(PaymentStatus.APPROVED, saved.status)
    }

    @Test
    @DisplayName("파트너별 결제 조회가 정상적으로 동작해야 한다")
    fun `파트너별 결제 조회가 정상적으로 동작해야 한다`() {
        // Given
        val payment1 = Payment(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardLast4 = "4242",
            approvalCode = "APPROVAL-123",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED
        )
        val payment2 = Payment(
            partnerId = 2L,
            amount = BigDecimal("20000"),
            appliedFeeRate = BigDecimal("0.0250"),
            feeAmount = BigDecimal("500"),
            netAmount = BigDecimal("19500"),
            cardLast4 = "1234",
            approvalCode = "APPROVAL-456",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED
        )

        paymentAdapter.save(payment1)
        paymentAdapter.save(payment2)

        // When
        val query = PaymentQuery(partnerId = 1L, limit = 10)
        val result = paymentAdapter.findBy(query)

        // Then
        assertEquals(1, result.items.size)
        assertEquals(1L, result.items[0].partnerId)
        assertEquals(BigDecimal("10000"), result.items[0].amount)
    }

    @Test
    @DisplayName("통계 조회가 정상적으로 동작해야 한다")
    fun `통계 조회가 정상적으로 동작해야 한다`() {
        // Given
        val payment1 = Payment(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardLast4 = "4242",
            approvalCode = "APPROVAL-123",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED
        )
        val payment2 = Payment(
            partnerId = 1L,
            amount = BigDecimal("20000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("700"),
            netAmount = BigDecimal("19300"),
            cardLast4 = "1234",
            approvalCode = "APPROVAL-456",
            approvedAt = LocalDateTime.of(2024, 1, 1, 0, 0),
            status = PaymentStatus.APPROVED
        )

        paymentAdapter.save(payment1)
        paymentAdapter.save(payment2)

        // When
        val filter = PaymentSummaryFilter(partnerId = 1L)
        val summary = paymentAdapter.summary(filter)

        // Then
        assertEquals(2, summary.count)
        assertEquals(BigDecimal("30000"), summary.totalAmount)
        assertEquals(BigDecimal("28900"), summary.totalNetAmount)
    }

    @Test
    @DisplayName("커서 기반 페이지네이션이 정상적으로 동작해야 한다")
    fun `커서 기반 페이지네이션이 정상적으로 동작해야 한다`() {
        // Given
        val baseTime = LocalDateTime.of(2024, 1, 1, 0, 0)
        val payment1 = Payment(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("400"),
            netAmount = BigDecimal("9600"),
            cardLast4 = "4242",
            approvalCode = "APPROVAL-123",
            approvedAt = baseTime,
            status = PaymentStatus.APPROVED,
            createdAt = baseTime.plusSeconds(3)
        )
        val payment2 = Payment(
            partnerId = 1L,
            amount = BigDecimal("20000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("700"),
            netAmount = BigDecimal("19300"),
            cardLast4 = "1234",
            approvalCode = "APPROVAL-456",
            approvedAt = baseTime,
            status = PaymentStatus.APPROVED,
            createdAt = baseTime.plusSeconds(2)
        )
        val payment3 = Payment(
            partnerId = 1L,
            amount = BigDecimal("30000"),
            appliedFeeRate = BigDecimal("0.0300"),
            feeAmount = BigDecimal("1000"),
            netAmount = BigDecimal("29000"),
            cardLast4 = "5678",
            approvalCode = "APPROVAL-789",
            approvedAt = baseTime,
            status = PaymentStatus.APPROVED,
            createdAt = baseTime.plusSeconds(1)
        )

        paymentAdapter.save(payment1)
        paymentAdapter.save(payment2)
        paymentAdapter.save(payment3)

        // When - 첫 번째 페이지 (limit=2)
        val firstPageQuery = PaymentQuery(partnerId = 1L, limit = 2)
        val firstPage = paymentAdapter.findBy(firstPageQuery)

        // Then - 첫 번째 페이지
        // createdAt desc 정렬이므로: payment1(3초), payment2(2초)가 반환됨
        // nextCursor는 마지막 항목인 payment2의 createdAt
        assertEquals(2, firstPage.items.size)
        assertTrue(firstPage.hasNext)
        assertEquals(baseTime.plusSeconds(2), firstPage.nextCursorCreatedAt)
        assertTrue(firstPage.nextCursorId != null)

        // When - 두 번째 페이지
        val secondPageQuery = PaymentQuery(
            partnerId = 1L,
            limit = 2,
            cursorCreatedAt = firstPage.nextCursorCreatedAt,
            cursorId = firstPage.nextCursorId
        )
        val secondPage = paymentAdapter.findBy(secondPageQuery)

        // Then - 두 번째 페이지
        assertEquals(1, secondPage.items.size)
        assertFalse(secondPage.hasNext)
        assertNull(secondPage.nextCursorCreatedAt)
        assertNull(secondPage.nextCursorId)
    }
}
