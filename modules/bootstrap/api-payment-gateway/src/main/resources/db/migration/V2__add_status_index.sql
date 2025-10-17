-- 상태별 조회 최적화를 위한 복합 인덱스 추가
-- status 필터와 함께 커서 페이징을 사용하는 쿼리 최적화
CREATE INDEX idx_payment_status_created ON payment (status, created_at DESC, id DESC);

