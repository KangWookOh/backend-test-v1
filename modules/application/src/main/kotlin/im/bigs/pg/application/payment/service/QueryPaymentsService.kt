package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryPaymentsUseCase
import im.bigs.pg.application.payment.port.`in`.QueryResult
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.domain.payment.PaymentSummary
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Base64

/**
 * 결제 이력 조회 유스케이스 구현체.
 * - 커서 토큰은 createdAt/id를 안전하게 인코딩해 전달/복원합니다.
 * - 통계는 조회 조건과 동일한 집합을 대상으로 계산됩니다.
 */
@Service
class QueryPaymentsService(
    private val paymentRepository: PaymentOutPort,
) : QueryPaymentsUseCase {
    /**
     * 필터를 기반으로 결제 내역을 조회합니다.
     *
     * @param filter 파트너/상태/기간/커서/페이지 크기
     * @return 조회 결과(목록/통계/커서)
     */
    override fun query(filter: QueryFilter): QueryResult {
        // 커서 디코딩
        val (cursorCreatedAt, cursorId) = decodeCursor(filter.cursor)

        // 상태 문자열을 enum으로 변환
        val status = filter.status?.let { try { PaymentStatus.valueOf(it.uppercase()) } catch (e: IllegalArgumentException) { null } }

        // 결제 내역 조회
        val paymentQuery = PaymentQuery(
            partnerId = filter.partnerId,
            status = status,
            from = filter.from,
            to = filter.to,
            cursorCreatedAt = cursorCreatedAt,
            cursorId = cursorId,
            limit = filter.limit,
        )

        val paymentPage = paymentRepository.findBy(paymentQuery)

        // 통계 조회 (동일한 필터 조건 적용)
        val summaryFilter = PaymentSummaryFilter(
            partnerId = filter.partnerId,
            status = status,
            from = filter.from,
            to = filter.to,
        )

        val summaryProjection = paymentRepository.summary(summaryFilter)
        val summary = PaymentSummary(
            count = summaryProjection.count,
            totalAmount = summaryProjection.totalAmount,
            totalNetAmount = summaryProjection.totalNetAmount,
        )

        // 다음 페이지 커서 생성
        val nextCursor = if (paymentPage.hasNext) {
            encodeCursor(paymentPage.nextCursorCreatedAt, paymentPage.nextCursorId)
        } else {
            null
        }

        return QueryResult(
            items = paymentPage.items,
            summary = summary,
            nextCursor = nextCursor,
            hasNext = paymentPage.hasNext,
        )
    }

    /** 다음 페이지 이동을 위한 커서 인코딩. */
    private fun encodeCursor(createdAt: java.time.LocalDateTime?, id: Long?): String? {
        if (createdAt == null || id == null) return null
        val instant = createdAt.atZone(java.time.ZoneOffset.UTC).toInstant()
        val raw = "${instant.toEpochMilli()}:$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())
    }

    /** 요청으로 전달된 커서 복원. 유효하지 않으면 null 커서로 간주합니다. */
    private fun decodeCursor(cursor: String?): Pair<java.time.LocalDateTime?, Long?> {
        if (cursor.isNullOrBlank()) return null to null
        return try {
            val raw = String(Base64.getUrlDecoder().decode(cursor))
            val parts = raw.split(":")
            val ts = parts[0].toLong()
            val id = parts[1].toLong()
            val instant = Instant.ofEpochMilli(ts)
            val localDateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneOffset.UTC)
            localDateTime to id
        } catch (e: Exception) {
            null to null
        }
    }
}
