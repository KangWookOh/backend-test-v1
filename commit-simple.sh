#!/bin/bash

# 간단 커밋 스크립트 - 현재 모든 변경사항을 의미있는 커밋으로 정리

set -e
cd /Users/kanguk/Desktop/kangwook/pay/backend-test-v1

echo "🚀 Git 커밋 시작..."

# 1. 도메인 레이어
echo "📦 1/8 - 도메인 모델 커밋..."
git add modules/domain/src/main/kotlin/
git add modules/domain/src/test/kotlin/
git add modules/domain/build.gradle.kts
git commit -m "feat: 도메인 모델 및 수수료 계산 로직 구현

- Payment, Partner, FeePolicy 도메인 모델
- PaymentStatus enum
- FeeCalculator: HALF_UP 반올림 수수료 계산
- CommissionCalculatorTest: 수수료 계산 단위 테스트"

# 2. 애플리케이션 포트 및 서비스
echo "🎯 2/8 - 애플리케이션 레이어 커밋..."
git add modules/application/src/main/kotlin/
git add modules/application/src/test/kotlin/
git add modules/application/build.gradle.kts
git commit -m "feat: 헥사고널 아키텍처 포트 및 유스케이스 구현

- PaymentUseCase, QueryPaymentsUseCase 정의
- PaymentService: 수수료 정책 적용 및 PG 연동
- QueryPaymentsService: 커서 페이징 및 통계 조회
- 포트 정의: PaymentOutPort, PartnerOutPort, FeePolicyOutPort, PgClientOutPort
- 통합 테스트 추가"

# 3. 인프라스트럭처 - 영속성
echo "💾 3/8 - 영속성 레이어 커밋..."
git add modules/infrastructure/persistence/src/main/kotlin/
git add modules/infrastructure/persistence/src/test/kotlin/
git add modules/infrastructure/persistence/build.gradle.kts
git commit -m "feat: JPA 영속성 어댑터 구현

- PaymentEntity, PartnerEntity, FeePolicyEntity
- PaymentPersistenceAdapter: 커서 기반 페이징 쿼리
- FeePolicyPersistenceAdapter: effective_from 기준 최신 정책 조회
- 인덱스 최적화: (created_at DESC, id DESC), (partner_id, effective_from DESC)
- PaymentRepositoryIntegrationTest: 페이징 통합 테스트"

# 4. 외부 연동 - PG Client
echo "🌐 4/8 - PG 클라이언트 커밋..."
git add modules/external/pg-client/src/main/kotlin/
git add modules/external/pg-client/build.gradle.kts
git commit -m "feat: PG 클라이언트 전략 패턴 구현

- MockPgClient: 홀수 partnerId 담당 (로컬 테스트용)
- TestPayPgClient: 짝수 partnerId 담당 (REST API 연동)
- 전략 패턴: supports() 기반 자동 선택"

# 5. API 컨트롤러 및 DTO
echo "🎮 5/8 - REST API 컨트롤러 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/
git commit -m "feat: REST API 컨트롤러 및 DTO 구현

- POST /api/v1/payments: 결제 생성
- GET /api/v1/payments: 결제 조회 (커서 페이징 + 통계)
- ISO-8601 날짜 형식 적용
- OpenAPI 어노테이션 추가
- @Validation 적용"

# 6. 설정 및 시드 데이터
echo "⚙️ 6/8 - 설정 및 초기화 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/config/
git add modules/bootstrap/api-payment-gateway/src/main/resources/
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/PgApiApplication.kt
git add modules/bootstrap/api-payment-gateway/build.gradle.kts
git commit -m "feat: Spring Boot 설정 및 선택과제 구현

- OpenApiConfig: Springdoc 설정
- DataInitializer: 시드 데이터 생성
- application.yml: H2, JPA, Actuator 설정
- application-prod.yml: MariaDB 프로필
- Actuator 엔드포인트: health, metrics, prometheus
- Swagger UI 활성화"

# 7. 빌드 설정
echo "🔧 7/8 - 빌드 설정 커밋..."
git add build.gradle.kts settings.gradle.kts gradle/ gradlew gradlew.bat .gitignore
git add gradle/
git commit -m "chore: Gradle 멀티모듈 프로젝트 설정

- Spring Boot 3.4.4 + Kotlin 1.9.25
- 모듈 구조: domain, application, infrastructure, external, bootstrap
- kotlin-jpa 플러그인 적용
- 의존성 버전 관리: libs.versions.toml
- Springdoc, Actuator 의존성 추가" || echo "이미 커밋됨"

# 8. 문서
echo "📝 8/8 - 문서화 커밋..."
git add GUIED.md README.md sql/
git commit -m "docs: 프로젝트 문서 작성

- GUIED.md: 구현 가이드 및 코드 리뷰 준비 자료
- README.md: 프로젝트 요구사항
- sql/scheme.sql: 데이터베이스 스키마" || echo "이미 커밋됨"

echo ""
echo "✅ 모든 커밋 완료!"
echo ""
echo "📊 커밋 히스토리:"
git log --oneline --graph | head -20

echo ""
echo "🎉 총 커밋 수:"
git log --oneline | wc -l



