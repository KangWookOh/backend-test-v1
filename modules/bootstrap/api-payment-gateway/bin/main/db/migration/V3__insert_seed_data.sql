-- 시드 데이터: Partner 및 수수료 정책
INSERT INTO partner (code, name, active) VALUES
  ('MOCK1', 'Mock Partner 1', TRUE),
  ('TESTPAY1', 'TestPay Partner 1', TRUE);

INSERT INTO partner_fee_policy (partner_id, effective_from, percentage, fixed_fee) VALUES
  (1, '2020-01-01 00:00:00', 0.0235, 0),
  (2, '2020-01-01 00:00:00', 0.0300, 100);

