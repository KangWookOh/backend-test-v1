#!/bin/bash

# Git 커밋 자동화 스크립트
# 사용법: chmod +x commit.sh && ./commit.sh

set -e  # 에러 발생 시 중단

echo "🚀 Git 커밋 시작..."
cd /Users/kanguk/Desktop/kangwook/pay/backend-test-v1

# 1. 초기 구조
echo "📦 1/22 - 초기 구조 커밋..."
git add .gitignore build.gradle.kts settings.gradle.kts gradle/ gradlew gradlew.bat
git add modules/domain/build.gradle.kts
git add modules/application/build.gradle.kts  
git add modules/infrastructure/persistence/build.gradle.kts
git add modules/external/pg-client/build.gradle.kts
git add modules/bootstrap/api-payment-gateway/build.gradle.kts
git add modules/bootstrap/api-payment-gateway/src/main/resources/application.yml
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/PgApiApplication.kt
git add sql/
git commit -m "chore: 프로젝트 초기 설정

- Gradle 멀티모듈 구조 설정
- 모듈별 의존성 정의 (domain, application, infrastructure, external, bootstrap)
- Spring Boot 3.4.4 + Kotlin 1.9.25
- H2, JPA, Jackson 기본 설정"

# 2. 도메인 모델
echo "🎯 2/22 - 도메인 모델 커밋..."
git add modules/domain/src/main/kotlin/
git commit -m "feat: 도메인 모델 정의

- Payment: 결제 도메인 모델
- PaymentStatus: 결제 상태 enum
- PaymentSummary: 통계 정보 모델
- Partner, FeePolicy: 제휴사 및 수수료 정책
- FeeCalculator: 순수 수수료 계산 유틸리티 (HALF_UP 반올림)"

# 3. 수수료 계산 테스트
echo "🧪 3/22 - 수수료 계산 테스트 커밋..."
git add modules/domain/src/test/
git commit -m "test: 수수료 계산 단위 테스트 추가

- 퍼센트 수수료 계산 검증
- 퍼센트 + 고정 수수료 조합 검증
- HALF_UP 반올림 검증"

# 4. Application 포트 정의
echo "🔌 4/22 - 포트 정의 커밋..."
git add modules/application/src/main/kotlin/im/bigs/pg/application/payment/port/
git add modules/application/src/main/kotlin/im/bigs/pg/application/partner/port/
git add modules/application/src/main/kotlin/im/bigs/pg/application/pg/port/
git commit -m "feat: 애플리케이션 포트 정의 (헥사고널 아키텍처)

- PaymentUseCase, QueryPaymentsUseCase: 입력 포트
- PaymentOutPort: 영속성 출력 포트
- PartnerOutPort, FeePolicyOutPort: 제휴사 정보 출력 포트
- PgClientOutPort: PG 연동 출력 포트
- 의존성 역전 원칙 적용"

# 5. 제휴사별 수수료 정책 조회 구현
echo "💰 5/22 - 수수료 정책 조회 커밋..."
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/partner/
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/config/
git commit -m "feat: 제휴사별 수수료 정책 조회 구현

- PartnerEntity, FeePolicyEntity: JPA 엔티티
- PartnerJpaRepository, FeePolicyJpaRepository: Spring Data JPA
- FeePolicyPersistenceAdapter: effective_from 기준 최신 정책 조회
- 복합 인덱스: (partner_id, effective_from DESC)"

# 6. MockPgClient 구현
echo "🎭 6/22 - MockPgClient 커밋..."
git add modules/external/pg-client/src/main/kotlin/im/bigs/pg/external/pg/MockPgClient.kt
git commit -m "feat: MockPgClient 구현

- 홀수 partnerId 담당
- 항상 승인 성공 반환
- 로컬 테스트용"

# 7. TestPay REST API 연동
echo "🌐 7/22 - TestPay API 연동 커밋..."
git add modules/external/pg-client/src/main/kotlin/im/bigs/pg/external/pg/TestPayPgClient.kt
git commit -m "feat: TestPay REST API 연동 구현

- 짝수 partnerId 담당
- RestTemplate으로 https://api-test-pg.bigs.im 호출
- 요청/응답 DTO snake_case 매핑
- 에러 핸들링 및 로깅"

# 8. 결제 생성 서비스
echo "💳 8/22 - 결제 생성 서비스 커밋..."
git add modules/application/src/main/kotlin/im/bigs/pg/application/payment/service/PaymentService.kt
git commit -m "feat: 결제 생성 서비스 수수료 정책 적용

- 제휴사 활성화 여부 검증
- PG 클라이언트 전략 패턴 (supports 기반 자동 선택)
- 수수료 정책 조회 및 적용 (effective_from 기준)
- FeeCalculator로 수수료/정산금 계산
- 하드코드 제거 완료"

# 9. 결제 서비스 테스트
echo "🧪 9/22 - 결제 서비스 테스트 커밋..."
git add modules/application/src/test/kotlin/im/bigs/pg/application/payment/service/결제서비스Test.kt
git commit -m "test: 결제 서비스 통합 테스트 추가

- MockK로 의존성 Mock
- 수수료 정책 적용 검증
- FeeCalculator 계산 결과 검증 (3% + 100원 = 400원)"

# 10. 결제 영속성 어댑터
echo "💾 10/22 - 결제 영속성 어댑터 커밋..."
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/payment/
git commit -m "feat: 결제 영속성 어댑터 구현

- PaymentEntity: JPA 엔티티
- PaymentJpaRepository: 커서 페이징 쿼리
- PaymentPersistenceAdapter: PaymentOutPort 구현
- 인덱스: (created_at DESC, id DESC), (partner_id, created_at DESC)"

# 11. 결제 조회 서비스
echo "🔍 11/22 - 결제 조회 서비스 커밋..."
git add modules/application/src/main/kotlin/im/bigs/pg/application/payment/service/QueryPaymentsService.kt
git commit -m "feat: 결제 조회 API 구현 (커서 페이징 + 통계)

- 커서 Base64 인코딩/디코딩
- PaymentStatus enum 변환 및 예외 처리
- 동일 필터로 items와 summary 조회 (일치 보장)
- nextCursor 생성"

# 12. 조회 서비스 테스트
echo "🧪 12/22 - 조회 서비스 테스트 커밋..."
git add modules/application/src/test/kotlin/im/bigs/pg/application/payment/service/QueryPaymentsServiceTest.kt
git commit -m "test: 결제 조회 서비스 단위 테스트 추가

- 빈 결과 처리
- 통계 집계 검증
- 커서 생성 검증
- 잘못된 상태값 처리"

# 13. 커서 페이징 통합 테스트
echo "🧪 13/22 - 커서 페이징 통합 테스트 커밋..."
git add modules/infrastructure/persistence/src/test/
git commit -m "test: 커서 페이징 통합 테스트 추가

- 35건 데이터 생성 후 페이징 테스트
- 첫 페이지 21건 조회
- 다음 페이지 커서 검증
- 통계 일치 검증 (count=35, total=35000, net=33950)"

# 14. REST API 컨트롤러
echo "🎮 14/22 - REST API 컨트롤러 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/
git commit -m "feat: REST API 컨트롤러 구현

- POST /api/v1/payments: 결제 생성
- GET /api/v1/payments: 결제 조회 + 통계
- DTO 분리 (Request, Response, QueryResponse)
- @Validation 적용
- OpenAPI 어노테이션 추가"

# 15. 시드 데이터
echo "🌱 15/22 - 시드 데이터 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/config/DataInitializer.kt
git commit -m "feat: 시드 데이터 초기화

- Partner 2개 생성 (MOCK1, TESTPAY1)
- 수수료 정책 생성 (2.35%, 3%+100원)
- CommandLineRunner로 시작 시 자동 실행"

# 16. Kotlin JPA 플러그인
echo "🔧 16/22 - Kotlin JPA 플러그인 커밋..."
git add build.gradle.kts
git commit -m "fix: Kotlin JPA 플러그인 적용

- infrastructure 모듈에 kotlin-jpa 플러그인 추가
- 엔티티 기본 생성자 자동 생성"

# 17. H2 런타임 의존성
echo "🗄️ 17/22 - H2 런타임 의존성 커밋..."
git add modules/bootstrap/api-payment-gateway/build.gradle.kts
git commit -m "fix: H2 데이터베이스 런타임 의존성 추가

- testImplementation → runtimeOnly 변경
- 애플리케이션 실행 시 H2 사용 가능"

# 18. 마지막 페이지 커서 null 처리
echo "🐛 18/22 - 커서 null 처리 커밋..."
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/payment/adapter/PaymentPersistenceAdapter.kt
git commit -m "fix: 마지막 페이지 커서 null 처리

- hasNext=false일 때 nextCursor null 반환
- 불필요한 커서 정보 제거"

# 19. ISO-8601 날짜 형식
echo "📅 19/22 - 날짜 형식 통일 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/dto/PaymentResponse.kt
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/PaymentController.kt
git commit -m "fix: ISO-8601 날짜 형식 통일

- @JsonFormat: yyyy-MM-dd'T'HH:mm:ss'Z'
- @DateTimeFormat: ISO.DATE_TIME
- README 예시와 일치"

# 20. OpenAPI 설정
echo "📚 20/22 - OpenAPI 설정 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/config/
git commit -m "feat: Springdoc OpenAPI 문서화 추가

- OpenApiConfig: API 정보 및 서버 설정
- Swagger UI 활성화
- 모든 API에 상세 설명 추가"

# 21. Actuator 설정
echo "📊 21/21 - Actuator 설정 커밋..."
git add modules/bootstrap/api-payment-gateway/src/main/resources/application.yml
git commit -m "feat: Spring Boot Actuator 운영지표 추가

- Health Check 엔드포인트
- Metrics 수집
- Prometheus 엔드포인트"

# 22. 문서화
echo "📝 22/22 - 문서화 커밋..."
git add GUIED.md README.md
git commit -m "docs: 구현 가이드 문서 작성

- 커밋 가이드
- 아키텍처 설명
- API 사용 예시
- 면접 준비 자료"

echo "✅ 모든 커밋 완료!"
echo ""
echo "📊 커밋 히스토리:"
git log --oneline --graph | head -30

echo ""
echo "🎉 완료! 총 커밋 수:"
git log --oneline | wc -l

