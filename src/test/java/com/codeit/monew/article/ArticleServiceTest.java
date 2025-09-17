package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
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
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

  Interest deafalutInterest;
  private Article defalutArticle1;
  private Article defalutArticle2;
  private Article defalutArticle3;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    now = LocalDateTime.now();
    // 공통 Interest
    deafalutInterest = Interest.builder()
        .id(UUID.randomUUID())
        .name("sports")
        .build();

    // 기사 3개 저장 (limit + 1)
    defalutArticle1 = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("A1")
        .articleViewCount(100)
        .articleCommentCount(10)
        .articlePublishDate(now.minusHours(1))
        .createdAt(now.minusHours(1))
        .source("NAVER")
        .interest(deafalutInterest)
        .deleted(false)
        .build();

    defalutArticle2 = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("A2")
        .articleViewCount(200)
        .articleCommentCount(20)
        .articlePublishDate(now.minusHours(2))
        .createdAt(now.minusHours(2))
        .source("NAVER")
        .interest(deafalutInterest)
        .deleted(false)
        .build();

    defalutArticle3 = Article.builder()
        .id(UUID.randomUUID())
        .articleTitle("A3")
        .articleViewCount(300)
        .articleCommentCount(30)
        .articlePublishDate(now.minusHours(3))
        .createdAt(now.minusHours(3))
        .source("NAVER")
        .interest(deafalutInterest)
        .deleted(false)
        .build();
  }


  @Test
  @DisplayName("기사 논리 삭제 성공 테스트")
  void softDeleteArticle_SuccessTest() {
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
  @DisplayName("해당하는 기사가 없어서 기사 논리 삭제 실패 테스트")
  void softDeleteArticle_FailTest() {
    // Given
    UUID id = UUID.randomUUID();
    given(articleRepository.findById(id)).willReturn(Optional.empty());

    // When
    Throwable thrown = catchThrowable(() -> articleService.softDeleteArticle(id));

    // Then
    assertThat(thrown)
        .isInstanceOf(ArticleNotFoundException.class);
  }

  @Test
  @DisplayName("기사 물리 삭제 성공 테스트")
  void hardDeleteArticle_SuccessTest() {
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

    // Then:
    then(articleRepository).should().delete(article);
    then(articleRepository).should().findById(id);
    then(articleRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("해당하는 기사가 없어서 기사 물리 삭제 실패 테스트")
  void hardDeleteArticle_FailTest() {
    // Given
    UUID id = UUID.randomUUID();
    given(articleRepository.findById(id)).willReturn(Optional.empty());

    // When
    Throwable thrown = catchThrowable(() -> articleService.hardDeleteArticle(id));

    // Then
    assertThat(thrown)
        .isInstanceOf(ArticleNotFoundException.class);
  }

  @Test
  @DisplayName("viewCount가 DESC인 정렬 기준 일때 기사 목록 조회 성공 테스트")
  void searchArticles_viewCount_DESC_SuccessTest() {
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

    // viewCount DESC 상위 2개가 나와야 하므로 1,2 반환
    List<Article> fetched = List.of(defalutArticle1, defalutArticle2);

    when(articleRepository.searchArticles(
        eq(keyword),
        eq(interestId),
        eq(sourceIn),
        eq(publishDateFrom),
        eq(publishDateTo),
        eq(orderBy),
        eq(direction),
        isNull(),       // cursor
        isNull(),       // after
        anyInt()        // limit
    )).thenReturn(fetched);

    // ★ requestUserId는 아래 서비스 호출에도 동일 객체/값 사용
    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();
    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));

    // existsByArticleAndUser는 같은 엔티티 인스턴스가 아닐 수 있으므로 ID 기반 매칭
    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle1.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(true);

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle2.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(false);

    // totalElements 스텁
    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    // Mapper 스텁: 실제로 사용하는 1,2만 스텁(불필요 스텁 경고 방지)
    when(articleMapper.toDto(defalutArticle1, true))
        .thenReturn(new ArticleDto(
            defalutArticle1.getId(), defalutArticle1.getSource(), defalutArticle1.getSourceUrl(),
            defalutArticle1.getArticleTitle(), defalutArticle1.getArticlePublishDate(),
            defalutArticle1.getArticleSummary(), defalutArticle1.getArticleCommentCount(),
            defalutArticle1.getArticleViewCount(), true
        ));

    when(articleMapper.toDto(defalutArticle2, false))
        .thenReturn(new ArticleDto(
            defalutArticle2.getId(), defalutArticle2.getSource(), defalutArticle2.getSourceUrl(),
            defalutArticle2.getArticleTitle(), defalutArticle2.getArticlePublishDate(),
            defalutArticle2.getArticleSummary(), defalutArticle2.getArticleCommentCount(),
            defalutArticle2.getArticleViewCount(), false
        ));

    // when
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then
    // viewCount DESC 기준: defalutArticle1(최고), defalutArticle2(다음)
    assertThat(res.content()).hasSize(2);
    assertThat(res.content().get(0).title()).isEqualTo("A1");
    assertThat(res.content().get(1).title()).isEqualTo("A2");
  }

  @Test
  @DisplayName("viewCount가 ASC인 정렬 기준 일때 기사 목록 조회 성공 테스트")
  void searchArticles_viewCount_ASC_SuccessTest() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "viewCount";
    String direction = "ASC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;

    List<Article> fetched = List.of(defalutArticle1, defalutArticle2, defalutArticle3);

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
    when(articleViewUserRepository.existsByArticleAndUser(defalutArticle1, user)).thenReturn(true);
    when(articleViewUserRepository.existsByArticleAndUser(defalutArticle2, user)).thenReturn(false);
    // article3는 초과분이라 호출되지 않음

    // totalElements 스텁
    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    when(articleMapper.toDto(defalutArticle1, true))
        .thenReturn(new ArticleDto(defalutArticle1.getId(), defalutArticle1.getSource(), defalutArticle1.getSourceUrl(), defalutArticle1.getArticleTitle(), defalutArticle1.getArticlePublishDate(), defalutArticle1.getArticleSummary(), defalutArticle1.getArticleCommentCount(), defalutArticle1.getArticleViewCount(), true));

    when(articleMapper.toDto(defalutArticle2, false))
        .thenReturn(new ArticleDto(defalutArticle2.getId(), defalutArticle2.getSource(), defalutArticle2.getSourceUrl(), defalutArticle2.getArticleTitle(), defalutArticle2.getArticlePublishDate(), defalutArticle2.getArticleSummary(), defalutArticle2.getArticleCommentCount(), defalutArticle2.getArticleViewCount(), false));

    when(articleMapper.toDto(defalutArticle3, false))
        .thenReturn(new ArticleDto(defalutArticle3.getId(), defalutArticle3.getSource(), defalutArticle3.getSourceUrl(), defalutArticle3.getArticleTitle(), defalutArticle3.getArticlePublishDate(), defalutArticle3.getArticleSummary(), defalutArticle3.getArticleCommentCount(), defalutArticle3.getArticleViewCount(), false));


    // when
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then
    assertThat(res.content().get(0).title()).isEqualTo("A1");    // 조회수가 가장 많은 기사
  }


  @Test
  @DisplayName("기사 뷰 DTO 조회 조회 이력 있을 때 성공 테스트")
  void findArticleViewDto_YesArticleUser_SuccessTest() {
    // Given
    UUID articleId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Article article = Article.builder()
        .id(articleId)
        .articleTitle("A1")
        .build();

    User user = User.builder()
        .id(userId)
        .nickname("tester")
        .build();

    LocalDateTime viewedAt = LocalDateTime.now().minusMinutes(5);

    ArticlesViewUser viewUser = ArticlesViewUser.builder()
        .article(article)
        .user(user)
        .createdAt(viewedAt)
        .build();

    // checkArticle / checkUser
    when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // 기존 조회 이력 있음
    when(articleViewUserRepository.findByArticleAndUser(article, user))
        .thenReturn(Optional.of(viewUser));

    // mapper가 반환할 DTO (구체 타입 몰라도 되도록 mock 객체로)
    ArticleViewDto expectedDto = mock(ArticleViewDto.class);
    when(articleViewMapper.toDto(article, userId, viewedAt)).thenReturn(expectedDto);

    // When
    ArticleViewDto result = articleService.getArticleViewDto(articleId, userId);

    // Then
    assertThat(result).isSameAs(expectedDto);
  }

  @Test
  @DisplayName("commentCount DESC 정렬 - 상위 2개 반환 (A3, A2)")
  void searchArticles_commentCount_DESC_Success() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "commentCount";
    String direction = "DESC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;

    // commentCount: A3(30) > A2(20) > A1(10)
    List<Article> fetched = List.of(defalutArticle3, defalutArticle2);

    when(articleRepository.searchArticles(
        eq(keyword), eq(interestId), eq(sourceIn),
        eq(publishDateFrom), eq(publishDateTo),
        eq(orderBy), eq(direction),
        isNull(), isNull(), anyInt()
    )).thenReturn(fetched);

    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();
    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));

    // viewed: 첫 번째 true, 두 번째 false (임의)
    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle3.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(true);

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle2.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(false);

    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    when(articleMapper.toDto(defalutArticle3, true)).thenReturn(new ArticleDto(
        defalutArticle3.getId(), defalutArticle3.getSource(), defalutArticle3.getSourceUrl(),
        defalutArticle3.getArticleTitle(), defalutArticle3.getArticlePublishDate(),
        defalutArticle3.getArticleSummary(), defalutArticle3.getArticleCommentCount(),
        defalutArticle3.getArticleViewCount(), true));

    when(articleMapper.toDto(defalutArticle2, false)).thenReturn(new ArticleDto(
        defalutArticle2.getId(), defalutArticle2.getSource(), defalutArticle2.getSourceUrl(),
        defalutArticle2.getArticleTitle(), defalutArticle2.getArticlePublishDate(),
        defalutArticle2.getArticleSummary(), defalutArticle2.getArticleCommentCount(),
        defalutArticle2.getArticleViewCount(), false));

    // when
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then (A3, A2)
    assertThat(res.content()).hasSize(2);
    assertThat(res.content().get(0).title()).isEqualTo("A3");
    assertThat(res.content().get(1).title()).isEqualTo("A2");
  }

  @Test
  @DisplayName("commentCount ASC 정렬 - 하위 2개 반환 (A1, A2)")
  void searchArticles_commentCount_ASC_Success() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "commentCount";
    String direction = "ASC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;

    // commentCount ASC: A1(10) < A2(20) < A3(30)
    List<Article> fetched = List.of(defalutArticle1, defalutArticle2);

    when(articleRepository.searchArticles(
        eq(keyword), eq(interestId), eq(sourceIn),
        eq(publishDateFrom), eq(publishDateTo),
        eq(orderBy), eq(direction),
        isNull(), isNull(), anyInt()
    )).thenReturn(fetched);

    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();
    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle1.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(true);

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle2.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(false);

    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    when(articleMapper.toDto(defalutArticle1, true)).thenReturn(new ArticleDto(
        defalutArticle1.getId(), defalutArticle1.getSource(), defalutArticle1.getSourceUrl(),
        defalutArticle1.getArticleTitle(), defalutArticle1.getArticlePublishDate(),
        defalutArticle1.getArticleSummary(), defalutArticle1.getArticleCommentCount(),
        defalutArticle1.getArticleViewCount(), true));

    when(articleMapper.toDto(defalutArticle2, false)).thenReturn(new ArticleDto(
        defalutArticle2.getId(), defalutArticle2.getSource(), defalutArticle2.getSourceUrl(),
        defalutArticle2.getArticleTitle(), defalutArticle2.getArticlePublishDate(),
        defalutArticle2.getArticleSummary(), defalutArticle2.getArticleCommentCount(),
        defalutArticle2.getArticleViewCount(), false));

    // when
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then (A1, A2)
    assertThat(res.content()).hasSize(2);
    assertThat(res.content().get(0).title()).isEqualTo("A1");
    assertThat(res.content().get(1).title()).isEqualTo("A2");
  }

  @Test
  @DisplayName("publishDate DESC 정렬(최신순) - 상위 2개 반환 (A1, A2)")
  void searchArticles_publishDate_DESC_Success() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "publishDate";
    String direction = "DESC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;

    // publishDate: A1(now-1h) > A2(now-2h) > A3(now-3h)
    List<Article> fetched = List.of(defalutArticle1, defalutArticle2);

    when(articleRepository.searchArticles(
        eq(keyword), eq(interestId), eq(sourceIn),
        eq(publishDateFrom), eq(publishDateTo),
        eq(orderBy), eq(direction),
        isNull(), isNull(), anyInt()
    )).thenReturn(fetched);

    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();
    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle1.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(true);

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle2.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(false);

    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    when(articleMapper.toDto(defalutArticle1, true)).thenReturn(new ArticleDto(
        defalutArticle1.getId(), defalutArticle1.getSource(), defalutArticle1.getSourceUrl(),
        defalutArticle1.getArticleTitle(), defalutArticle1.getArticlePublishDate(),
        defalutArticle1.getArticleSummary(), defalutArticle1.getArticleCommentCount(),
        defalutArticle1.getArticleViewCount(), true));

    when(articleMapper.toDto(defalutArticle2, false)).thenReturn(new ArticleDto(
        defalutArticle2.getId(), defalutArticle2.getSource(), defalutArticle2.getSourceUrl(),
        defalutArticle2.getArticleTitle(), defalutArticle2.getArticlePublishDate(),
        defalutArticle2.getArticleSummary(), defalutArticle2.getArticleCommentCount(),
        defalutArticle2.getArticleViewCount(), false));

    // when
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then (A1, A2)
    assertThat(res.content()).hasSize(2);
    assertThat(res.content().get(0).title()).isEqualTo("A1");
    assertThat(res.content().get(1).title()).isEqualTo("A2");
  }

  @Test
  @DisplayName("publishDate ASC 정렬(오래된순) - 하위 2개 반환 (A3, A2)")
  void searchArticles_publishDate_ASC_Success() {
    // given
    String keyword = "스포츠";
    UUID interestId = UUID.randomUUID();
    List<String> sourceIn = List.of("NAVER");
    LocalDateTime publishDateFrom = LocalDateTime.now().minusDays(7);
    LocalDateTime publishDateTo = LocalDateTime.now();
    String orderBy = "publishDate";
    String direction = "ASC";
    String cursor = null;
    LocalDateTime after = null;
    int limit = 2;

    // publishDate ASC: A3(now-3h) < A2(now-2h) < A1(now-1h)
    List<Article> fetched = List.of(defalutArticle3, defalutArticle2);

    when(articleRepository.searchArticles(
        eq(keyword), eq(interestId), eq(sourceIn),
        eq(publishDateFrom), eq(publishDateTo),
        eq(orderBy), eq(direction),
        isNull(), isNull(), anyInt()
    )).thenReturn(fetched);

    UUID requestUserId = UUID.randomUUID();
    User user = User.builder().id(requestUserId).nickname("test").build();
    when(userRepository.findById(requestUserId)).thenReturn(Optional.of(user));

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle3.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(true);

    when(articleViewUserRepository.existsByArticleAndUser(
        argThat(a -> a != null && a.getId().equals(defalutArticle2.getId())),
        argThat(u -> u != null && u.getId().equals(user.getId()))
    )).thenReturn(false);

    when(articleRepository.searchArticlesCount(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo
    )).thenReturn(3L);

    when(articleMapper.toDto(defalutArticle3, true)).thenReturn(new ArticleDto(
        defalutArticle3.getId(), defalutArticle3.getSource(), defalutArticle3.getSourceUrl(),
        defalutArticle3.getArticleTitle(), defalutArticle3.getArticlePublishDate(),
        defalutArticle3.getArticleSummary(), defalutArticle3.getArticleCommentCount(),
        defalutArticle3.getArticleViewCount(), true));

    when(articleMapper.toDto(defalutArticle2, false)).thenReturn(new ArticleDto(
        defalutArticle2.getId(), defalutArticle2.getSource(), defalutArticle2.getSourceUrl(),
        defalutArticle2.getArticleTitle(), defalutArticle2.getArticlePublishDate(),
        defalutArticle2.getArticleSummary(), defalutArticle2.getArticleCommentCount(),
        defalutArticle2.getArticleViewCount(), false));

    // when
    CursorPageResponseArticleDto res = articleService.articleSearch(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo,
        orderBy, direction, cursor, after, limit, requestUserId
    );

    // then (A3, A2)
    assertThat(res.content()).hasSize(2);
    assertThat(res.content().get(0).title()).isEqualTo("A3");
    assertThat(res.content().get(1).title()).isEqualTo("A2");
  }


  @Test
  @DisplayName("기사 뷰 DTO 조회 조회 이력 없을 때 성공 테스트")
  void findArticleViewDto_NoArticleUser_SuccessTest() {
    // Given
    UUID articleId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Article article = Article.builder()
        .id(articleId)
        .articleTitle("A2")
        .build();

    User user = User.builder()
        .id(userId)
        .nickname("tester2")
        .build();

    // checkArticle / checkUser
    when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // 기존 이력 없음 → 새로 생성
    when(articleViewUserRepository.findByArticleAndUser(article, user))
        .thenReturn(Optional.empty());

    // createViewUserSafely 내부 구현을 모를 때는 save 동작만 스텁하면 충분
    LocalDateTime newViewedAt = LocalDateTime.now();
    ArticlesViewUser savedViewUser = ArticlesViewUser.builder()
        .article(article)
        .user(user)
        .createdAt(newViewedAt)
        .build();

    when(articleViewUserRepository.save(any(ArticlesViewUser.class)))
        .thenReturn(savedViewUser);

    ArticleViewDto expectedDto = mock(ArticleViewDto.class);
    when(articleViewMapper.toDto(article, userId, newViewedAt)).thenReturn(expectedDto);

    // When
    ArticleViewDto result = articleService.getArticleViewDto(articleId, userId);

    // Then
    assertThat(result).isSameAs(expectedDto);
  }

  @Test
  @DisplayName("유저가 없어서 기사 뷰 DTO 조회 실패 테스트")
  void findArticleViewDto_NoUser_SuccessTest() {
    // Given
    UUID articleId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    given(articleRepository.findById(articleId)).willReturn(Optional.empty());

    // When
    Throwable thrown = catchThrowable(() -> articleService.getArticleViewDto(articleId, userId));

    // Then
    assertThat(thrown)
        .isInstanceOf(ArticleNotFoundException.class);
  }

  @Test
  @DisplayName("기사가 없어서 기사 뷰 DTO 조회 실패 테스트")
  void findArticleViewDto_NoArticle_SuccessTest() {
    // Given
    UUID articleId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Article article = Article.builder().id(articleId).build();
    given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // When
    Throwable thrown = catchThrowable(() -> articleService.getArticleViewDto(articleId, userId));

    // Then
    assertThat(thrown)
        .isInstanceOf(UserException.class);
  }

  @Test
  @DisplayName("source검색")
  void findSources() {
    // given
    List<String> sources = List.of("Naver", "ChoSun", "HanKyung");
    when(articleRepository.findDistinctSources()).thenReturn(sources);

    // when
    List<String> result = articleService.findSources();

    // then
    assertThat(result).containsExactlyElementsOf(sources);
  }
}
