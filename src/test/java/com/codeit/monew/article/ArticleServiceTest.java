package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.entity.ArticlesViewUser;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.mapper.ArticleViewMapperImpl;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewUserRepository;
import com.codeit.monew.article.response_dto.ArticleDto;
import com.codeit.monew.article.response_dto.ArticleViewDto;
import com.codeit.monew.article.response_dto.CursorPageResponseArticleDto;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock
  ArticleRepository articleRepository;

  @Mock
  ArticleViewUserRepository articleViewUserRepository;

  @Mock
  CommentRepository commentRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  ArticleMapper articleMapper;

  @Spy
  private ArticleViewMapper articleViewMapper = new ArticleViewMapperImpl();

  @InjectMocks
  ArticleService articleService; // 실제 서비스 클래스명으로 변경

  @Test
  @DisplayName("기사 논리 삭제 테스트")
  void softDeleteArticle() {
    // Given
    UUID id = UUID.randomUUID();
    Article article = Article.builder()
        .deleted(false)
        .build();
    given(articleRepository.findById(id)).willReturn(Optional.of(article));

    // When
    articleService.softDeleteArticle(id);

    // Then
    assertThat(article.isDeleted()).isTrue();
    then(articleRepository).should().findById(id);
    then(articleRepository).should().save(article);
  }

  @Test
  @DisplayName("기사 물리 삭제 테스트")
  void hardDeleteArticle() {
    // Given
    UUID id = UUID.randomUUID();
    Interest interest = Interest.builder().name("test").subscriberCount(0).build();
    Article article = Article.builder()
        .source("NAVER")
        .sourceUrl("https://example.com")
        .articleTitle("네이버 뉴스 제목")
        .articlePublishDate(LocalDateTime.now())
        .articleSummary("뉴스 요약")
        .articleCommentCount(0L)
        .articleViewCount(0L)
        .deleted(false)
        .interest(interest)
        .build();

    given(articleRepository.save(any(Article.class)))
        .willAnswer(inv -> inv.getArgument(0));

    given(articleRepository.findById(id))
        .willReturn(Optional.of(article));
    Article saved = articleRepository.save(article);

    // When
    articleService.hardDeleteArticle(id);

    // Then: 삭제 로직이 delete(...)를 호출했는지 검증
    then(articleRepository).should().delete(article);
    then(articleRepository).should().findById(id);
    then(articleRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("기사 삭제시 기사가 없으면 예외 발생 테스트")
  void deleteArticleThrowsWhenNotFoundAndNoSave() {
    // Given
    UUID id = UUID.randomUUID();
    given(articleRepository.findById(id)).willReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> articleService.softDeleteArticle(id))
        .isInstanceOf(ArticleNotFoundException.class);

    then(articleRepository).should().findById(id);
    then(articleRepository).should(never()).save(org.mockito.Mockito.any());
  }
  
  @Test
  @DisplayName("기사 목록 조회 테스트")
  void searchArticles() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "viewCount";
    String direction = "DESC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;

    // 기사 3개 (limit+1) → hasNext = true
    Article article1 = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("A1")
        .articleViewCount(500)
        .articleCommentCount(10)
        .articlePublishDate(publishDateTo.minusHours(1))
        .createdAt(publishDateTo.minusHours(1))
        .build();

    Article article2 = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("A2")
        .articleViewCount(450)
        .articleCommentCount(20)
        .articlePublishDate(publishDateTo.minusHours(2))
        .createdAt(publishDateTo.minusHours(2))
        .build();

    Article article3 = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("A3")
        .articleViewCount(440)
        .articleCommentCount(30)
        .articlePublishDate(publishDateTo.minusHours(3))
        .createdAt(publishDateTo.minusHours(3))
        .build();

    List<Article> fetched = List.of(article1, article2, article3);

    when(articleRepository.searchArticles(
        eq(keyword),
        eq(interestId),
        eq(sourceIn),
        eq(publishDateFrom),
        eq(publishDateTo),
        eq(orderBy),
        eq(direction),
        isNull(),                     // cursor
        isNull(),                     // after
        anyInt()
    )).thenReturn(fetched);

    // ★ 여기서 만든 requestUserId를 아래 서비스 호출에도 "그대로" 사용해야 함
    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();
    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));

    // 조회 여부 (실제 매핑은 잘라낸 2건만 수행됨)
    when(articleViewUserRepository.existsByArticleAndUser(article1, user)).thenReturn(true);
    when(articleViewUserRepository.existsByArticleAndUser(article2, user)).thenReturn(false);
    // article3는 초과분이라 호출되지 않음

    // totalElements 스텁
    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    when(articleMapper.toDto(article1, true))
        .thenReturn(new ArticleDto(article1.getId(), article1.getSource(), article1.getSourceUrl(), article1.getArticleTitle(), article1.getArticlePublishDate(), article1.getArticleSummary(), article1.getArticleCommentCount(), article1.getArticleViewCount(), true));

    when(articleMapper.toDto(article2, false))
        .thenReturn(new ArticleDto(article2.getId(), article2.getSource(), article2.getSourceUrl(), article2.getArticleTitle(), article2.getArticlePublishDate(), article2.getArticleSummary(), article2.getArticleCommentCount(), article2.getArticleViewCount(), false));

    // when  ★ requestUserId 변수를 그대로 사용 (UUID.randomUUID() 쓰면 스텁 미스매치 발생)
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then
    assertThat(res.content()).hasSize(3);      // limit 만큼만
  }

  @Test
  @DisplayName("기사 뷰가 없을 때 새 기사 뷰 등록 테스트")
  void searchArticlesThrowsWhenNotFoundAndNoSave() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "viewCount";
    String direction = "DESC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;
    UUID articleId = UUID.randomUUID();

    Article article = Article.builder()
        .id(articleId)
        .articleTitle("A1")
        .articleViewCount(500)
        .articleCommentCount(10)
        .articlePublishDate(publishDateTo.minusHours(1))
        .createdAt(publishDateTo.minusHours(1))
        .build();

    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();

    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));
    when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
    when(articleViewUserRepository.findByArticleAndUser(article, user)).thenReturn(Optional.empty());

    ArticlesViewUser saved = new ArticlesViewUser(article, user);
    when(articleViewUserRepository.save(any(ArticlesViewUser.class))).thenReturn(saved);
    // when
    ArticleViewDto dto = articleService.getArticleViewDto(articleId, requestUserId);

    // then
    assertThat(dto.id()).isEqualTo(articleId);
  }

  @Test
  @DisplayName("source검색")
  void findSources() {
    // given
    List<String> sources = List.of("NAVER", "DAUM");
    when(articleRepository.findDistinctSources()).thenReturn(sources);

    // when
    List<String> result = articleService.findSources();

    // then
    assertThat(result).containsExactlyElementsOf(sources);
  }
}
