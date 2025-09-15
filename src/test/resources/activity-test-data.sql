-- ====================================
-- UserActivity Service Test Data
-- UUID 형식으로 변경된 테스트 데이터
-- ====================================

-- 1. 테스트 사용자들 (ACTIVE 상태)
INSERT INTO users (id, email, nickname, password, status, deleted_at, created_at, updated_at) VALUES
                                                                                                  ('550e8400-e29b-41d4-a716-446655440001', 'testuser@test.com', 'testUser', '$2a$12$test.password.hash', 'ACTIVE', NULL, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
                                                                                                  ('550e8400-e29b-41d4-a716-446655440002', 'user2@test.com', 'user2', '$2a$12$test.password.hash', 'ACTIVE', NULL, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
                                                                                                  ('550e8400-e29b-41d4-a716-446655440003', 'deleteduser@test.com', 'deletedUser', '$2a$12$test.password.hash', 'DELETED', '2024-01-01 12:00:00', '2024-01-01 10:00:00', '2024-01-01 12:00:00');

-- 2. 관심사들
INSERT INTO interests (id, name, subscriber_count , is_deleted, created_at, updated_at) VALUES
                                                                                            ('6ba7b810-9dad-11d1-80b4-00c04fd430c1', 'Technology', 150, false, '2024-01-01 09:00:00', '2024-01-01 09:00:00'),
                                                                                            ('6ba7b811-9dad-11d1-80b4-00c04fd430c1', 'Sports', 80, false, '2024-01-01 09:00:00', '2024-01-01 09:00:00'),
                                                                                            ('6ba7b812-9dad-11d1-80b4-00c04fd430c1', 'Travel', 120, false, '2024-01-01 09:00:00', '2024-01-01 09:00:00');

-- 3. 키워드들
INSERT INTO keywords (id, keyword, is_deleted, interest_id, created_at, updated_at) VALUES
                                                                                        ('f47ac10b-58cc-4372-a567-0e02b2c3d471', 'Java', false, '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '2024-01-01 09:30:00', '2024-01-01 09:30:00'),
                                                                                        ('f47ac10b-58cc-4372-a567-0e02b2c3d472', 'Spring Boot', false, '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '2024-01-01 09:30:00', '2024-01-01 09:30:00'),
                                                                                        ('f47ac10b-58cc-4372-a567-0e02b2c3d473', 'Football', false, '6ba7b811-9dad-11d1-80b4-00c04fd430c1', '2024-01-01 09:30:00', '2024-01-01 09:30:00'),
                                                                                        ('f47ac10b-58cc-4372-a567-0e02b2c3d474', 'Baseball', false, '6ba7b811-9dad-11d1-80b4-00c04fd430c1', '2024-01-01 09:30:00', '2024-01-01 09:30:00'),
                                                                                        ('f47ac10b-58cc-4372-a567-0e02b2c3d475', 'Europe', false, '6ba7b812-9dad-11d1-80b4-00c04fd430c1', '2024-01-01 09:30:00', '2024-01-01 09:30:00');

-- 4. 구독 관계 (testUser가 Technology와 Sports 구독)
INSERT INTO subscriptions (id, user_id, interest_id, created_at) VALUES
                                                                     ('123e4567-e89b-12d3-a456-426614174001', '550e8400-e29b-41d4-a716-446655440001', '6ba7b810-9dad-11d1-80b4-00c04fd430c1', '2024-01-02 10:00:00'),
                                                                     ('123e4567-e89b-12d3-a456-426614174002', '550e8400-e29b-41d4-a716-446655440001', '6ba7b811-9dad-11d1-80b4-00c04fd430c1', '2024-01-02 11:00:00'),
                                                                     ('123e4567-e89b-12d3-a456-426614174003', '550e8400-e29b-41d4-a716-446655440002', '6ba7b812-9dad-11d1-80b4-00c04fd430c1', '2024-01-02 12:00:00');

-- 5. 기사들
INSERT INTO articles (id, source, source_url, article_title, article_publish_date, article_summary, article_comment_count, article_view_count, created_At, is_deleted, interest_id) VALUES
                                                                                                                                                                                        ('9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d', 'TechNews', 'https://technews.com/java-spring', 'Java Spring Framework 최신 업데이트', '2024-01-03 09:00:00', 'Spring Framework의 새로운 기능들을 소개합니다', 5, 120, '2024-01-03 09:00:00', false, '6ba7b810-9dad-11d1-80b4-00c04fd430c1'),
                                                                                                                                                                                        ('9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6e', 'SportsTimes', 'https://sportstimes.com/football', '월드컵 결승 리뷰', '2024-01-03 14:00:00', '월드컵 결승전 하이라이트와 분석', 12, 350, '2024-01-03 14:00:00', false, '6ba7b811-9dad-11d1-80b4-00c04fd430c1'),
                                                                                                                                                                                        ('9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6f', 'TravelMag', 'https://travelmag.com/europe', '유럽 여행 가이드 2024', '2024-01-03 16:00:00', '2024년 유럽 여행 필수 정보', 8, 200, '2024-01-03 16:00:00', false, '6ba7b812-9dad-11d1-80b4-00c04fd430c1');

-- 6. 기사 조회 기록 (testUser가 기사들을 조회한 기록)
INSERT INTO articles_view_user (id, article_id, user_id, created_at) VALUES
                                                                         ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d', '550e8400-e29b-41d4-a716-446655440001', '2024-01-03 10:00:00'),
                                                                         ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6e', '550e8400-e29b-41d4-a716-446655440001', '2024-01-03 15:00:00'),
                                                                         ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6f', '550e8400-e29b-41d4-a716-446655440001', '2024-01-03 17:00:00'),
                                                                         ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d', '550e8400-e29b-41d4-a716-446655440002', '2024-01-03 11:00:00');

-- 7. 댓글들 (testUser가 작성한 댓글들)
INSERT INTO comments (id, content, deleted, like_count, deleted_at, user_id, article_id, created_at) VALUES
                                                                                                         ('c9bf9e57-1685-4c89-bafb-ff5af830be8a', 'Spring Framework 정말 유용하네요!', false, 3, NULL, '550e8400-e29b-41d4-a716-446655440001', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d', '2024-01-03 10:30:00'),
                                                                                                         ('c9bf9e57-1685-4c89-bafb-ff5af830be8b', '월드컵 경기 정말 감동적이었습니다', false, 5, NULL, '550e8400-e29b-41d4-a716-446655440001', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6e', '2024-01-03 15:30:00'),
                                                                                                         ('c9bf9e57-1685-4c89-bafb-ff5af830be8c', '유럽 여행 정보 감사합니다', false, 2, NULL, '550e8400-e29b-41d4-a716-446655440001', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6f', '2024-01-03 17:30:00'),
                                                                                                         ('c9bf9e57-1685-4c89-bafb-ff5af830be8d', '좋은 프레임워크 정보네요', false, 1, NULL, '550e8400-e29b-41d4-a716-446655440002', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d', '2024-01-03 11:30:00'),
                                                                                                         ('c9bf9e57-1685-4c89-bafb-ff5af830be8e', '추가 정보도 부탁드려요', false, 0, NULL, '550e8400-e29b-41d4-a716-446655440001', '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d', '2024-01-03 18:00:00');

-- 8. 댓글 좋아요들 (testUser가 누른 좋아요들)
INSERT INTO comment_likes (id, comment_id, user_id, created_at) VALUES
                                                                    ('d290f1ee-6c54-4b01-90e6-d701748f0851', 'c9bf9e57-1685-4c89-bafb-ff5af830be8d', '550e8400-e29b-41d4-a716-446655440001', '2024-01-03 12:00:00'),
                                                                    ('d290f1ee-6c54-4b01-90e6-d701748f0852', 'c9bf9e57-1685-4c89-bafb-ff5af830be8a', '550e8400-e29b-41d4-a716-446655440002', '2024-01-03 10:45:00'),
                                                                    ('d290f1ee-6c54-4b01-90e6-d701748f0853', 'c9bf9e57-1685-4c89-bafb-ff5af830be8b', '550e8400-e29b-41d4-a716-446655440002', '2024-01-03 15:45:00');
