#!/bin/bash

# 안전한 순차 커밋 스크립트

set -e
cd /Users/kanguk/Desktop/kangwook/pay/backend-test-v1

echo "🚀 Git 커밋 시작..."

# 1. 초기 구조 (Gradle 설정)
echo "📦 1/11 - 초기 Gradle 설정..."
git add .gitignore
git add build.gradle.kts settings.gradle.kts
git add gradle/
git add gradlew gradlew.bat
git add gradle/libs.versions.toml
git add sql/
git commit -m "chore: Gradle 멀티모듈 프로젝트 초기 설정

- Spring Boot 3.4.4 + Kotlin 1.9.25
- 멀티모듈 구조 정의
- 의존성 버전 관리 (libs.versions.toml)
- Springdoc, Actuator 의존성 추가"

# 2. 모듈별 빌드 설정
echo "📦 2/11 - 모듈별 빌드 설정..."
git add modules/domain/build.gradle.kts
git add modules/application/build.gradle.kts  
git add modules/infrastructure/persistence/build.gradle.kts
git add modules/external/pg-client/build.gradle.kts
git add modules/bootstrap/api-payment-gateway/build.gradle.kts
git commit -m "chore: 모듈별 build.gradle.kts 설정

- domain: 순수 도메인 로직
- application: 유스케이스 및 포트
- infrastructure/persistence: JPA 영속성
- external/pg-client: PG 연동
- bootstrap/api-payment-gateway: Spring Boot 진입점
- kotlin-jpa 플러그인 적용"

# 3. 도메인 모델
echo "🎯 3/11 - 도메인 모델 및 계산 로직..."
git add modules/domain/src/main/kotlin/
git add modules/domain/src/test/kotlin/
git commit -m "feat: 도메인 모델 및 수수료 계산 로직

- Payment, PaymentStatus: 결제 도메인
- PaymentSummary: 통계 모델
- Partner, FeePolicy: 제휴사 및 수수료 정책
- FeeCalculator: HALF_UP 반올림 수수료 계산
- CommissionCalculatorTest: 단위 테스트"

# 4. 애플리케이션 포트 및 유스케이스
echo "🔌 4/11 - 애플리케이션 레이어..."
git add modules/application/src/main/kotlin/
git add modules/application/src/test/kotlin/
git commit -m "feat: 헥사고널 아키텍처 포트 및 유스케이스

- 입력 포트: PaymentUseCase, QueryPaymentsUseCase
- 출력 포트: PaymentOutPort, PartnerOutPort, FeePolicyOutPort, PgClientOutPort
- PaymentService: 수수료 정책 적용 및 PG 연동
- QueryPaymentsService: 커서 페이징 및 통계
- 유스케이스 테스트"

# 5. 인프라스트럭처 - JPA 영속성
echo "💾 5/11 - JPA 영속성 어댑터..."
git add modules/infrastructure/persistence/src/main/kotlin/
git add modules/infrastructure/persistence/src/test/kotlin/
git commit -m "feat: JPA 영속성 어댑터 구현

- PaymentEntity, PartnerEntity, FeePolicyEntity
- PaymentPersistenceAdapter: 커서 페이징
- FeePolicyPersistenceAdapter: effective_from 기준 최신 정책
- JpaConfig: @EnableJpaRepositories
- 인덱스 최적화 및 통합 테스트"

# 6. 외부 연동 - PG Client
echo "🌐 6/11 - PG 클라이언트 전략 패턴..."
git add modules/external/pg-client/src/main/kotlin/
git commit -m "feat: PG 클라이언트 전략 패턴 구현

- MockPgClient: 홀수 partnerId (로컬용)
- TestPayPgClient: 짝수 partnerId (REST API)
- supports() 기반 자동 선택"

# 7. REST API - DTO
echo "📦 7/11 - REST API DTO..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/dto/
git commit -m "feat: REST API DTO 정의

- CreatePaymentRequest: 결제 생성 요청
- PaymentResponse: 결제 응답
- QueryDtos: 조회 요청/응답
- ISO-8601 날짜 형식"

# 8. REST API - 컨트롤러
echo "🎮 8/11 - REST API 컨트롤러..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/PaymentController.kt
git commit -m "feat: REST API 컨트롤러

- POST /api/v1/payments: 결제 생성
- GET /api/v1/payments: 결제 조회 (커서 페이징 + 통계)
- OpenAPI 어노테이션
- @Validation 적용"

# 9. Spring Boot 설정
echo "⚙️ 9/11 - Spring Boot 설정..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/config/
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/PgApiApplication.kt
git add modules/bootstrap/api-payment-gateway/src/main/resources/
git commit -m "feat: Spring Boot 설정 및 선택과제

- PgApiApplication: 메인 클래스
- OpenApiConfig: Springdoc 설정
- DataInitializer: 시드 데이터 (MOCK1, TESTPAY1)
- application.yml: H2, JPA, Actuator
- application-prod.yml: MariaDB 프로필"

# 10. 빌드 산출물 (bin 디렉터리)
echo "🔧 10/11 - 빌드 산출물..."
git add modules/*/bin/ 2>/dev/null || true
git add modules/*/*/bin/ 2>/dev/null || true
git commit -m "build: Kotlin 컴파일 산출물

- bin/ 디렉터리: IDE 빌드 결과
- 각 모듈별 컴파일된 클래스 및 리소스" || echo "산출물 없음, 스킵"

# 11. 문서 및 스크립트
echo "📝 11/11 - 문서화..."
git add README.md GUIED.md
git add docker-compose.yml
git add commit*.sh
git commit -m "docs: 프로젝트 문서 및 도구

- GUIED.md: 구현 가이드 및 코드 리뷰 자료
- README.md: 프로젝트 요구사항
- docker-compose.yml: MariaDB 컨테이너 (선택과제)
- commit 스크립트: Git 커밋 자동화 도구" || echo "이미 커밋됨, 스킵"

# 남은 변경사항 정리
echo ""
echo "🧹 남은 변경사항 정리..."
git add -A
git commit -m "chore: 나머지 변경사항 정리

- 파일 삭제 및 수정사항
- .gitignore에 포함되지 않은 파일들" || echo "정리할 것 없음"

echo ""
echo "✅ 모든 커밋 완료!"
echo ""
echo "📊 커밋 히스토리:"
git log --oneline --graph | head -15

echo ""
echo "🎉 총 커밋 수:"
git log --oneline | wc -l

echo ""
echo "📝 남은 파일:"
git status --short

