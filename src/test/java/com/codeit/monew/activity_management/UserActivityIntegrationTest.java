package com.codeit.monew.activity_management;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.monew.activity_management.response_dto.CommentActivityDto;
import com.codeit.monew.activity_management.response_dto.CommentLikeActivityDto;
import com.codeit.monew.activity_management.response_dto.UserActivityDto;
import com.codeit.monew.activity_management.response_dto.UserArticleViewDto;
import com.codeit.monew.activity_management.response_dto.UserSubscriptionDto;
import com.codeit.monew.activity_management.service.UserActivityService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@Sql("/activity-test-data.sql")
@SpringBootTest
public class UserActivityIntegrationTest {

  @Autowired
  UserActivityService userActivityService;

  @Test
  @DisplayName("유저의 활동을 조회한다.")
  public void getUserActivity() {
    // given
    UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    // when
    UserActivityDto result = userActivityService.getUserActivity(userId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.id().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(result.email()).isEqualTo("testuser@test.com");
    assertThat(result.nickname()).isEqualTo("testUser");
    assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));

    // Then - 구독 정보 검증
    assertThat(result.subscriptions()).hasSize(2);

    // Technology 구독 검증
    UserSubscriptionDto techSubscription = result.subscriptions().get(0);
    assertThat(techSubscription.id().toString()).isEqualTo("123e4567-e89b-12d3-a456-426614174001");
    assertThat(techSubscription.interestId().toString()).isEqualTo("6ba7b810-9dad-11d1-80b4-00c04fd430c1");
    assertThat(techSubscription.interestName()).isEqualTo("Technology");
    assertThat(techSubscription.interestKeywords()).containsExactly("Java", "Spring Boot");
    assertThat(techSubscription.interestSubscriberCount()).isEqualTo(150);
    assertThat(techSubscription.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));

    // Sports 구독 검증
    UserSubscriptionDto sportsSubscription = result.subscriptions().get(1);
    assertThat(sportsSubscription.id().toString()).isEqualTo("123e4567-e89b-12d3-a456-426614174002");
    assertThat(sportsSubscription.interestId().toString()).isEqualTo("6ba7b811-9dad-11d1-80b4-00c04fd430c1");
    assertThat(sportsSubscription.interestName()).isEqualTo("Sports");
    assertThat(sportsSubscription.interestKeywords()).containsExactly("Football", "Baseball");
    assertThat(sportsSubscription.interestSubscriberCount()).isEqualTo(80);
    assertThat(sportsSubscription.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 11, 0));

    // Then - 댓글 활동 검증 (4개)
    assertThat(result.comments()).hasSize(4);

    // 첫 번째 댓글 (Java Spring Framework 추가 정보 요청)
    CommentActivityDto firstComment = result.comments().get(0);
    assertThat(firstComment.id().toString()).isEqualTo("c9bf9e57-1685-4c89-bafb-ff5af830be8e");
    assertThat(firstComment.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d");
    assertThat(firstComment.articleTitle()).isEqualTo("Java Spring Framework 최신 업데이트");
    assertThat(firstComment.userId().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(firstComment.userNickname()).isEqualTo("testUser");
    assertThat(firstComment.content()).isEqualTo("추가 정보도 부탁드려요");
    assertThat(firstComment.likeCount()).isEqualTo(0);
    assertThat(firstComment.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 18, 0));

    // 두 번째 댓글 (유럽 여행 정보 감사)
    CommentActivityDto secondComment = result.comments().get(1);
    assertThat(secondComment.id().toString()).isEqualTo("c9bf9e57-1685-4c89-bafb-ff5af830be8c");
    assertThat(secondComment.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6f");
    assertThat(secondComment.articleTitle()).isEqualTo("유럽 여행 가이드 2024");
    assertThat(secondComment.userId().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(secondComment.userNickname()).isEqualTo("testUser");
    assertThat(secondComment.content()).isEqualTo("유럽 여행 정보 감사합니다");
    assertThat(secondComment.likeCount()).isEqualTo(2);
    assertThat(secondComment.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 17, 30));

    // 세 번째 댓글 (월드컵 경기 감동)
    CommentActivityDto thirdComment = result.comments().get(2);
    assertThat(thirdComment.id().toString()).isEqualTo("c9bf9e57-1685-4c89-bafb-ff5af830be8b");
    assertThat(thirdComment.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6e");
    assertThat(thirdComment.articleTitle()).isEqualTo("월드컵 결승 리뷰");
    assertThat(thirdComment.userId().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(thirdComment.userNickname()).isEqualTo("testUser");
    assertThat(thirdComment.content()).isEqualTo("월드컵 경기 정말 감동적이었습니다");
    assertThat(thirdComment.likeCount()).isEqualTo(5);
    assertThat(thirdComment.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 15, 30));

    // 네 번째 댓글 (Spring Framework 유용)
    CommentActivityDto fourthComment = result.comments().get(3);
    assertThat(fourthComment.id().toString()).isEqualTo("c9bf9e57-1685-4c89-bafb-ff5af830be8a");
    assertThat(fourthComment.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d");
    assertThat(fourthComment.articleTitle()).isEqualTo("Java Spring Framework 최신 업데이트");
    assertThat(fourthComment.userId().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(fourthComment.userNickname()).isEqualTo("testUser");
    assertThat(fourthComment.content()).isEqualTo("Spring Framework 정말 유용하네요!");
    assertThat(fourthComment.likeCount()).isEqualTo(3);
    assertThat(fourthComment.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 10, 30));

    // Then - 댓글 좋아요 활동 검증 (1개)
    assertThat(result.commentLikes()).hasSize(1);

    CommentLikeActivityDto commentLike = result.commentLikes().get(0);
    assertThat(commentLike.id().toString()).isEqualTo("d290f1ee-6c54-4b01-90e6-d701748f0851");
    assertThat(commentLike.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 12, 0));
    assertThat(commentLike.commentId().toString()).isEqualTo("c9bf9e57-1685-4c89-bafb-ff5af830be8d");
    assertThat(commentLike.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d");
    assertThat(commentLike.articleTitle()).isEqualTo("Java Spring Framework 최신 업데이트");
    assertThat(commentLike.commentUserId().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(commentLike.commentUserNickname()).isEqualTo("testUser");
    assertThat(commentLike.commentContent()).isEqualTo("좋은 프레임워크 정보네요");
    assertThat(commentLike.commentLikeCount()).isEqualTo(1);
    assertThat(commentLike.commentCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 11, 30));

    // Then - 기사 조회 활동 검증 (3개)
    assertThat(result.articleViews()).hasSize(3);

    // 첫 번째 조회 (유럽 여행 가이드)
    UserArticleViewDto firstView = result.articleViews().get(0);
    assertThat(firstView.id().toString()).isEqualTo("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13");
    assertThat(firstView.viewedBy().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(firstView.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 17, 0));
    assertThat(firstView.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6f");
    assertThat(firstView.source()).isEqualTo("TravelMag");
    assertThat(firstView.sourceUrl()).isEqualTo("https://travelmag.com/europe");
    assertThat(firstView.articleTitle()).isEqualTo("유럽 여행 가이드 2024");
    assertThat(firstView.articlePublishDate()).isEqualTo(LocalDateTime.of(2024, 1, 3, 16, 0));
    assertThat(firstView.articleSummary()).isEqualTo("2024년 유럽 여행 필수 정보");
    assertThat(firstView.articleCommentCount()).isEqualTo(8);
    assertThat(firstView.articleViewCount()).isEqualTo(200);

    // 두 번째 조회 (월드컵 결승 리뷰)
    UserArticleViewDto secondView = result.articleViews().get(1);
    assertThat(secondView.id().toString()).isEqualTo("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12");
    assertThat(secondView.viewedBy().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(secondView.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 15, 0));
    assertThat(secondView.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6e");
    assertThat(secondView.source()).isEqualTo("SportsTimes");
    assertThat(secondView.sourceUrl()).isEqualTo("https://sportstimes.com/football");
    assertThat(secondView.articleTitle()).isEqualTo("월드컵 결승 리뷰");
    assertThat(secondView.articlePublishDate()).isEqualTo(LocalDateTime.of(2024, 1, 3, 14, 0));
    assertThat(secondView.articleSummary()).isEqualTo("월드컵 결승전 하이라이트와 분석");
    assertThat(secondView.articleCommentCount()).isEqualTo(12);
    assertThat(secondView.articleViewCount()).isEqualTo(350);

    // 세 번째 조회 (Java Spring Framework)
    UserArticleViewDto thirdView = result.articleViews().get(2);
    assertThat(thirdView.id().toString()).isEqualTo("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    assertThat(thirdView.viewedBy().toString()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");
    assertThat(thirdView.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 3, 10, 0));
    assertThat(thirdView.articleId().toString()).isEqualTo("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d");
    assertThat(thirdView.source()).isEqualTo("TechNews");
    assertThat(thirdView.sourceUrl()).isEqualTo("https://technews.com/java-spring");
    assertThat(thirdView.articleTitle()).isEqualTo("Java Spring Framework 최신 업데이트");
    assertThat(thirdView.articlePublishDate()).isEqualTo(LocalDateTime.of(2024, 1, 3, 9, 0));
    assertThat(thirdView.articleSummary()).isEqualTo("Spring Framework의 새로운 기능들을 소개합니다");
    assertThat(thirdView.articleCommentCount()).isEqualTo(5);
    assertThat(thirdView.articleViewCount()).isEqualTo(120);
  }


}
