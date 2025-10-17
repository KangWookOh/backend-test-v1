# Git 커밋 가이드

> 의미 있는 커밋 히스토리를 위한 단계별 가이드

## 🎯 커밋 전략

작은 단위로 나눠서 커밋 → 각 커밋은 하나의 의미 있는 작업 단위

---

## 📌 커밋 순서

### 1️⃣ 초기 구조 (chore)

**커밋 메시지**:
```bash
git add .gitignore build.gradle.kts settings.gradle.kts gradle/ gradlew gradlew.bat
git add modules/domain/build.gradle.kts
git add modules/application/build.gradle.kts  
git add modules/infrastructure/persistence/build.gradle.kts
git add modules/external/pg-client/build.gradle.kts
git add modules/bootstrap/api-payment-gateway/build.gradle.kts
git add modules/bootstrap/api-payment-gateway/src/main/resources/application.yml
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/PgApiApplication.kt
git commit -m "chore: 프로젝트 초기 설정

- Gradle 멀티모듈 구조 설정
- 모듈별 의존성 정의 (domain, application, infrastructure, external, bootstrap)
- Spring Boot 3.4.4 + Kotlin 1.9.25
- H2, JPA, Jackson 기본 설정"
```

---

### 2️⃣ 도메인 모델 (feat)

**커밋 메시지**:
```bash
git add modules/domain/src/main/kotlin/im/bigs/pg/domain/
git commit -m "feat: 도메인 모델 정의

- Payment: 결제 도메인 모델
- PaymentStatus: 결제 상태 enum
- PaymentSummary: 통계 정보 모델
- Partner, FeePolicy: 제휴사 및 수수료 정책
- FeeCalculator: 순수 수수료 계산 유틸리티 (HALF_UP 반올림)"
```

---

### 3️⃣ 수수료 계산 테스트 (test)

**커밋 메시지**:
```bash
git add modules/domain/src/test/kotlin/im/bigs/pg/domain/calculation/CommissionCalculatorTest.kt
git commit -m "test: 수수료 계산 단위 테스트 추가

- 퍼센트 수수료 계산 검증
- 퍼센트 + 고정 수수료 조합 검증
- HALF_UP 반올림 검증"
```

---

### 4️⃣ Application 포트 정의 (feat)

**커밋 메시지**:
```bash
git add modules/application/src/main/kotlin/im/bigs/pg/application/payment/port/
git add modules/application/src/main/kotlin/im/bigs/pg/application/partner/port/
git add modules/application/src/main/kotlin/im/bigs/pg/application/pg/port/
git commit -m "feat: 애플리케이션 포트 정의 (헥사고널 아키텍처)

- PaymentUseCase, QueryPaymentsUseCase: 입력 포트
- PaymentOutPort: 영속성 출력 포트
- PartnerOutPort, FeePolicyOutPort: 제휴사 정보 출력 포트
- PgClientOutPort: PG 연동 출력 포트
- 의존성 역전 원칙 적용"
```

---

### 5️⃣ 제휴사별 수수료 정책 조회 구현 (feat)

**커밋 메시지**:
```bash
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/partner/
git commit -m "feat: 제휴사별 수수료 정책 조회 구현

- PartnerEntity, FeePolicyEntity: JPA 엔티티
- PartnerJpaRepository, FeePolicyJpaRepository: Spring Data JPA
- FeePolicyPersistenceAdapter: effective_from 기준 최신 정책 조회
- 복합 인덱스: (partner_id, effective_from DESC)"
```

---

### 6️⃣ PG 연동 구현 (feat)

**커밋 메시지**:
```bash
git add modules/external/pg-client/src/main/kotlin/im/bigs/pg/external/pg/MockPgClient.kt
git commit -m "feat: MockPgClient 구현

- 홀수 partnerId 담당
- 항상 승인 성공 반환
- 로컬 테스트용"
```

---

### 7️⃣ TestPay REST API 연동 구현 (feat)

**커밋 메시지**:
```bash
git add modules/external/pg-client/src/main/kotlin/im/bigs/pg/external/pg/TestPayPgClient.kt
git commit -m "feat: TestPay REST API 연동 구현

- 짝수 partnerId 담당
- RestTemplate으로 https://api-test-pg.bigs.im 호출
- 요청/응답 DTO snake_case 매핑
- 에러 핸들링 및 로깅"
```

---

### 8️⃣ 결제 생성 서비스 구현 (feat)

**커밋 메시지**:
```bash
git add modules/application/src/main/kotlin/im/bigs/pg/application/payment/service/PaymentService.kt
git commit -m "feat: 결제 생성 서비스 수수료 정책 적용

- 제휴사 활성화 여부 검증
- PG 클라이언트 전략 패턴 (supports 기반 자동 선택)
- 수수료 정책 조회 및 적용 (effective_from 기준)
- FeeCalculator로 수수료/정산금 계산
- 하드코드 제거 완료"
```

---

### 9️⃣ 결제 서비스 테스트 (test)

**커밋 메시지**:
```bash
git add modules/application/src/test/kotlin/im/bigs/pg/application/payment/service/결제서비스Test.kt
git commit -m "test: 결제 서비스 통합 테스트 추가

- MockK로 의존성 Mock
- 수수료 정책 적용 검증
- FeeCalculator 계산 결과 검증 (3% + 100원 = 400원)"
```

---

### 🔟 결제 영속성 어댑터 구현 (feat)

**커밋 메시지**:
```bash
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/payment/
git commit -m "feat: 결제 영속성 어댑터 구현

- PaymentEntity: JPA 엔티티
- PaymentJpaRepository: 커서 페이징 쿼리
- PaymentPersistenceAdapter: PaymentOutPort 구현
- 인덱스: (created_at DESC, id DESC), (partner_id, created_at DESC)"
```

---

### 1️⃣1️⃣ 커서 페이징 쿼리 구현 (feat)

**커밋 메시지**:
```bash
# 이미 위에 포함되었으므로 별도 커밋 불필요
# 하지만 강조하고 싶다면:
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/payment/repository/PaymentJpaRepository.kt
git commit -m "feat: 커서 기반 페이징 쿼리 구현

- pageBy: (created_at, id) 복합 커서 조건
- summary: 필터 조건과 동일한 집합 집계
- limit+1 패턴으로 다음 페이지 존재 여부 판단"
```

---

### 1️⃣2️⃣ 결제 조회 서비스 구현 (feat)

**커밋 메시지**:
```bash
git add modules/application/src/main/kotlin/im/bigs/pg/application/payment/service/QueryPaymentsService.kt
git commit -m "feat: 결제 조회 API 구현 (커서 페이징 + 통계)

- 커서 Base64 인코딩/디코딩
- PaymentStatus enum 변환 및 예외 처리
- 동일 필터로 items와 summary 조회 (일치 보장)
- nextCursor 생성"
```

---

### 1️⃣3️⃣ 조회 서비스 테스트 (test)

**커밋 메시지**:
```bash
git add modules/application/src/test/kotlin/im/bigs/pg/application/payment/service/QueryPaymentsServiceTest.kt
git commit -m "test: 결제 조회 서비스 단위 테스트 추가

- 빈 결과 처리
- 통계 집계 검증
- 커서 생성 검증
- 잘못된 상태값 처리"
```

---

### 1️⃣4️⃣ 커서 페이징 통합 테스트 (test)

**커밋 메시지**:
```bash
git add modules/infrastructure/persistence/src/test/kotlin/im/bigs/pg/infra/persistence/PaymentRepositoryPagingTest.kt
git commit -m "test: 커서 페이징 통합 테스트 추가

- 35건 데이터 생성 후 페이징 테스트
- 첫 페이지 21건 조회
- 다음 페이지 커서 검증
- 통계 일치 검증 (count=35, total=35000, net=33950)"
```

---

### 1️⃣5️⃣ REST API 컨트롤러 구현 (feat)

**커밋 메시지**:
```bash
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/
git commit -m "feat: REST API 컨트롤러 구현

- POST /api/v1/payments: 결제 생성
- GET /api/v1/payments: 결제 조회 + 통계
- DTO 분리 (Request, Response, QueryResponse)
- @Validation 적용"
```

---

### 1️⃣6️⃣ 시드 데이터 구성 (feat)

**커밋 메시지**:
```bash
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/config/DataInitializer.kt
git commit -m "feat: 시드 데이터 초기화

- Partner 2개 생성 (MOCK1, TESTPAY1)
- 수수료 정책 생성 (2.35%, 3%+100원)
- CommandLineRunner로 시작 시 자동 실행"
```

---

### 1️⃣7️⃣ JPA 설정 추가 (fix)

**커밋 메시지**:
```bash
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/config/JpaConfig.kt
git commit -m "fix: JPA 설정 추가

- @EnableJpaRepositories, @EntityScan
- 패키지 스캔 범위 설정"
```

---

### 1️⃣8️⃣ Kotlin JPA 플러그인 적용 (fix)

**커밋 메시지**:
```bash
git add build.gradle.kts
git commit -m "fix: Kotlin JPA 플러그인 적용

- infrastructure 모듈에 kotlin-jpa 플러그인 추가
- 엔티티 기본 생성자 자동 생성"
```

---

### 1️⃣9️⃣ H2 데이터베이스 런타임 의존성 추가 (fix)

**커밋 메시지**:
```bash
git add modules/bootstrap/api-payment-gateway/build.gradle.kts
git commit -m "fix: H2 데이터베이스 런타임 의존성 추가

- testImplementation → runtimeOnly 변경
- 애플리케이션 실행 시 H2 사용 가능"
```

---

### 2️⃣0️⃣ 마지막 페이지 커서 null 처리 (fix)

**커밋 메시지**:
```bash
git add modules/infrastructure/persistence/src/main/kotlin/im/bigs/pg/infra/persistence/payment/adapter/PaymentPersistenceAdapter.kt
git commit -m "fix: 마지막 페이지 커서 null 처리

- hasNext=false일 때 nextCursor null 반환
- 불필요한 커서 정보 제거"
```

---

### 2️⃣1️⃣ ISO-8601 날짜 형식 통일 (fix)

**커밋 메시지**:
```bash
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/dto/PaymentResponse.kt
git add modules/bootstrap/api-payment-gateway/src/main/kotlin/im/bigs/pg/api/payment/PaymentController.kt
git commit -m "fix: ISO-8601 날짜 형식 통일

- @JsonFormat: yyyy-MM-dd'T'HH:mm:ss'Z'
- @DateTimeFormat: ISO.DATE_TIME
- README 예시와 일치"
```

---

### 2️⃣2️⃣ 스키마 및 인덱스 정의 (docs)

**커밋 메시지**:
```bash
git add sql/scheme.sql
git commit -m "docs: 데이터베이스 스키마 정의

- partner, partner_fee_policy, payment 테이블
- 인덱스: 커서 페이징, 제휴사별 조회 최적화"
```

---

### 2️⃣3️⃣ 구현 문서 작성 (docs)

**커밋 메시지**:
```bash
git add IMPLEMENTATION.md
git commit -m "docs: 구현 내역 문서 작성

- 아키텍처 설명
- 핵심 의사결정
- API 사용 예시
- 테스트 전략
- 면접 대비 포인트"
```

---

## 🚀 실행 방법

### 옵션 1: 한 번에 모든 커밋 실행
```bash
cd /Users/kanguk/Desktop/kangwook/pay/backend-test-v1

# 위의 커밋들을 순서대로 복사-붙여넣기
```

### 옵션 2: 스크립트로 실행
아래 스크립트를 `commit.sh`로 저장 후 실행:

```bash
#!/bin/bash
cd /Users/kanguk/Desktop/kangwook/pay/backend-test-v1

# 1. 초기 구조
git add .gitignore build.gradle.kts settings.gradle.kts gradle/ gradlew gradlew.bat
git add modules/*/build.gradle.kts modules/bootstrap/*/build.gradle.kts
git commit -m "chore: 프로젝트 초기 설정"

# 2. 도메인 모델
git add modules/domain/src/main/
git commit -m "feat: 도메인 모델 정의"

# 3. 테스트
git add modules/domain/src/test/
git commit -m "test: 수수료 계산 단위 테스트 추가"

# ... (나머지 커밋)
```

---

## 📝 주의사항

1. **커밋 전 확인**
   ```bash
   git status  # 변경된 파일 확인
   git diff    # 변경 내용 확인
   ```

2. **테스트 실행**
   ```bash
   ./gradlew test  # 각 커밋 후 테스트 통과 확인
   ```

3. **커밋 메시지 규칙**
   - feat: 새로운 기능
   - fix: 버그 수정
   - test: 테스트 추가
   - docs: 문서 작성
   - chore: 기타 (빌드, 설정 등)

---

**작성일**: 2025-10-14  
**총 커밋 수**: 약 20개  
**예상 소요 시간**: 30분

