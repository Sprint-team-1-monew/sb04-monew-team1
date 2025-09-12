package com.codeit.monew.article;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.naver.NaverNewsCollector;
import com.codeit.monew.article.naver.NaverNewsItem;
import com.codeit.monew.article.naver.NaverNewsResponse;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)

class NaverNewsCollectorTest {


  WebClient webClient;
  @Mock
  ArticleRepository articleRepository;
  @Mock
  InterestRepository interestRepository;
  @Mock
  KeywordRepository keywordRepository;
  MockWebServer server;

  // WebClient 체인 목
  @Mock WebClient.RequestHeadersUriSpec<?> uriSpec;
  @Mock WebClient.RequestHeadersSpec<?> headersSpec;
  @Mock WebClient.ResponseSpec responseSpec;

  @InjectMocks
  NaverNewsCollector collector;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();

    // MockWebServer 주소로 baseUrl 구성
    webClient = WebClient.builder()
        .baseUrl(server.url("/").toString())
        .build();
    collector = new NaverNewsCollector(webClient, articleRepository, interestRepository, keywordRepository);
   }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  private boolean invokeDuplicate(Article article) throws Exception {
    Method m = NaverNewsCollector.class.getDeclaredMethod("isArticleDuplicated", Article.class);
    m.setAccessible(true);
    return (boolean) m.invoke(collector, article);
  }

  private Article invokeBuild(NaverNewsItem item, Interest interest) throws Exception {
    Method m = NaverNewsCollector.class.getDeclaredMethod("buildArticleFromNaverItem",
        NaverNewsItem.class, Interest.class);
    m.setAccessible(true);
    return (Article) m.invoke(collector, item, interest);
  }

  @Test
  @DisplayName("searchNews: 성공(200) 시 JSON을 NaverNewsResponse로 역직렬화하고 요청 쿼리 파라미터가 검증")
  void searchNews_success() throws InterruptedException {
    // given: 네이버 뉴스 API 응답 형태에 맞춘 최소 예시
    String body = """
      {
        "lastBuildDate": "Tue, 02 Sep 2025 12:00:00 +0900",
        "total": 2,
        "start": 1,
        "display": 10,
        "items": [
          {
            "title": "첫 기사",
            "originallink": "https://example.com/1",
            "link": "https://n.news.naver.com/article/0000001",
            "description": "본문1",
            "pubDate": "Tue, 02 Sep 2025 11:00:00 +0900"
          },
          {
            "title": "둘 기사",
            "originallink": "https://example.com/2",
            "link": "https://n.news.naver.com/article/0000002",
            "description": "본문2",
            "pubDate": "Tue, 02 Sep 2025 10:30:00 +0900"
          }
        ]
      }
      """;

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(body));

    String interest = "스포츠";
    Integer display = 10;
    Integer start = 1;
    String sort = "date";

    // when
    NaverNewsResponse resp = collector.searchNews(interest, display, start, sort);

    // then: 응답 역직렬화 확인
    assertThat(resp).isNotNull();
    assertThat(resp.items()).hasSize(2);
    NaverNewsItem first = resp.items().get(0);
    assertThat(first.title()).isEqualTo("첫 기사");
    assertThat(first.originallink()).isEqualTo("https://example.com/1");
    assertThat(first.link()).startsWith("https://n.news.naver.com/");
    assertThat(first.description()).isEqualTo("본문1");
    assertThat(first.pubDate()).isEqualTo("Tue, 02 Sep 2025 11:00:00 +0900");

    // 요청 검증: 경로/쿼리 파라미터가 정확한지 확인
    RecordedRequest req = server.takeRequest();
    assertThat(req.getMethod()).isEqualTo("GET");
    assertThat(req.getPath()).startsWith("/v1/search/news.json");

    // 간단히 포함 여부로 검증 (URL 인코딩 때문에 한글 파라미터는 전체 일치 대신 contains 사용)
    String path = req.getRequestUrl().encodedPath() + "?" + req.getRequestUrl().encodedQuery();
    assertThat(path).contains("display=10");
    assertThat(path).contains("start=1");
    assertThat(path).contains("sort=date");
    assertThat(path).contains("query="); // 쿼리 키 존재
    // 한글은 인코딩되어 오므로 존재만 확인 (예: %EC%8A%A4%ED%8F%AC%EC%B8%A0)
    assertThat(req.getRequestUrl().queryParameter("query")).isEqualTo(interest);
  }

  @Test
  @DisplayName("키워드가 null/blank면 무시되고, 여러 번 등장해도 모두 강조됨")
  void build_ignores_blank_keywords_and_highlights_all_occurrences() throws Exception {
    Interest interest = Interest.builder()
        .name("스포츠")
        .subscriberCount(Integer.valueOf(1))
        .isDeleted(false)
        .build();

    when(keywordRepository.findByInterest(interest))
        .thenReturn(List.of(
            Keyword.builder().keyword("  ").deletedAt(false).interest(interest).build(),
            Keyword.builder().keyword(null).deletedAt(false).interest(interest).build(),
            Keyword.builder().keyword("축구").deletedAt(false).interest(interest).build()
        ));

    NaverNewsItem item = mock(NaverNewsItem.class);
    when(item.description()).thenReturn("축구, 축구, 축구아님, 축구, 추구, 출구");
    when(item.originallink()).thenReturn("o");
    when(item.pubDate()).thenReturn("Tue, 02 Sep 2025 12:00:00 +0900");

    Article article = invokeBuild(item, interest);
    System.out.println("기사" + article.getArticleSummary());

    assertThat(article.getArticleSummary())
        .contains("<b>축구</b>, <b>축구</b>, <b>축구</b>아님, <b>축구</b>, 추구, 출구");
  }

  @Test
  @DisplayName("중복 기사 존재: findBySourceUrl가 값을 반환하면 true")
  void isArticle_Duplicated_returnsTrue_whenExists() throws Exception {
    // given
    String url = "https://origin1.example/news/123";
    Article article = Mockito.mock(Article.class);
    when(article.getSourceUrl()).thenReturn(url);

    when(articleRepository.findBySourceUrl(url))
        .thenReturn(Optional.of(article)); // 존재한다고 가정

    // when
    boolean dup = invokeDuplicate(article);

    // then
    assertThat(dup).isTrue();
    verify(articleRepository, times(1)).findBySourceUrl(url);
  }

  @Test
  @DisplayName("중복 기사 없음: findBySourceUrl가 empty이면 false")
  void isArticle_Duplicated_returnsFalse_whenNotExists() throws Exception {
    // given
    String url = "https://origin2.example/news/999";
    Article article = Mockito.mock(Article.class);
    when(article.getSourceUrl()).thenReturn(url);

    when(articleRepository.findBySourceUrl(url))
        .thenReturn(Optional.empty()); // 없음

    // when
    boolean dup = invokeDuplicate(article);

    // then
    assertThat(dup).isFalse();
    verify(articleRepository, times(1)).findBySourceUrl(url);
  }
}