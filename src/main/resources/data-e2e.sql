INSERT INTO hospital (
    hospital_id, hospital_name, hospital_addr, hospital_number,
    open_status, open_time, close_time, lunch_start_time, lunch_end_time,
    closed_days, waiting_people, waiting_time, rating_sum, rating_count
) VALUES (
    1, 'FORHOS 통합병원', '서울특별시 강남구 테헤란로 123', '02-1234-5678',
    TRUE, TIME '00:00:00', TIME '23:59:59', NULL, NULL,
    '', 0, 0, 48, 10
);

INSERT INTO member (
    member_id, email, password, name, age, phone, create_at,
    gender, region, role, hospital_id,
    medicines, diseases, allergies, medical_notes, extra
) VALUES
    (
        1, 'user@forhos.test', '$2a$10$YIol2MyT71U9n7sAocMqAe5J8bX/dVR8L67gUjVo2v67kEcCwB.KW',
        '통합사용자', 30, '010-0000-0001', CURRENT_TIMESTAMP,
        'MALE', '서울', 'USER', NULL,
        '', '', '', '', 'E2E 일반 회원'
    ),
    (
        2, 'admin@forhos.test', '$2a$10$YIol2MyT71U9n7sAocMqAe5J8bX/dVR8L67gUjVo2v67kEcCwB.KW',
        '통합관리자', 35, '010-0000-0002', CURRENT_TIMESTAMP,
        'FEMALE', '서울', 'HOSPITAL_ADMIN', 1,
        '', '', '', '', 'E2E 병원 관리자'
    );
