package im.bigs.pg.api.config

import im.bigs.pg.infra.persistence.partner.entity.FeePolicyEntity
import im.bigs.pg.infra.persistence.partner.entity.PartnerEntity
import im.bigs.pg.infra.persistence.partner.repository.FeePolicyJpaRepository
import im.bigs.pg.infra.persistence.partner.repository.PartnerJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.Instant

/**
 * 시드 데이터 초기화 (Deprecated).
 * - Flyway 마이그레이션(V3__insert_seed_data.sql)으로 대체됨
 * - H2 프로필에서만 동작 (Flyway baseline 전 상태)
 * - 프로덕션에서는 Flyway만 사용
 */
@Configuration
class DataInitializer {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seed(
        partnerRepo: PartnerJpaRepository,
        feeRepo: FeePolicyJpaRepository,
    ) = CommandLineRunner {
        // ✅ Flyway 마이그레이션이 실행되었으면 스킵
        // (V3에서 이미 시드 데이터 삽입)
        if (partnerRepo.count() > 0L) {
            log.info("시드 데이터가 이미 존재합니다 (Flyway 마이그레이션 완료)")
            return@CommandLineRunner
        }

        // ⚠️ 레거시: Flyway 미사용 환경에서만 실행
        val p1 = partnerRepo.save(PartnerEntity(code = "MOCK1", name = "Mock Partner 1", active = true))
        val p2 = partnerRepo.save(PartnerEntity(code = "TESTPAY1", name = "TestPay Partner 1", active = true))
        feeRepo.save(
            FeePolicyEntity(
                partnerId = p1.id!!,
                effectiveFrom = Instant.parse("2020-01-01T00:00:00Z"),
                percentage = BigDecimal("0.0235"),
                fixedFee = BigDecimal.ZERO,
            ),
        )
        feeRepo.save(
            FeePolicyEntity(
                partnerId = p2.id!!,
                effectiveFrom = Instant.parse("2020-01-01T00:00:00Z"),
                percentage = BigDecimal("0.0300"),
                fixedFee = BigDecimal("100"),
            ),
        )
        log.info("시드 데이터 생성 완료 (레거시 모드): partners {} and {}", p1.id, p2.id)
    }
}
