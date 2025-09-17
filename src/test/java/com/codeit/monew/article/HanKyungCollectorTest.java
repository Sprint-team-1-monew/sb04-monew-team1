package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.rss.HankyungCollector;
import com.codeit.monew.article.rss.RssItem;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
public class HanKyungCollectorTest {

  @Mock
  private WebClient webClient;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private KeywordRepository keywordRepository;

  @InjectMocks
  private HankyungCollector hankyungCollector;

  @Test
  @DisplayName("한국 경제 테스트")
  void hankyung_saves_whenMatch_andNotDuplicated() throws Exception {
    // given
    HankyungCollector spyCollector = spy(new HankyungCollector(
        null, articleRepository, interestRepository, keywordRepository));

    Interest interest = Interest.builder().name("상속세").build();
    when(interestRepository.findAll()).thenReturn(List.of(interest));
    when(keywordRepository.findByInterest(interest)).thenReturn(List.of());

    LocalDateTime now = LocalDateTime.now();
    RssItem item = new RssItem(
        "대선 때 18억까지 상속세 면제 공약",
        "https://www.hankyung.com/politics/2025/09/11/ABC123/",
        now
    );

    doReturn(List.of(item)).when(spyCollector).getAllRss();
    doReturn("요약 상속세 포함").when(spyCollector).getArticleSummary(item.link());

    when(articleRepository.findBySourceUrl(item.link())).thenReturn(Optional.empty());

    when(articleRepository.save(any(Article.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // when
    spyCollector.hankyungArticleCollect();

    // then — 호출 횟수만 검증(인자 무시)
    verify(articleRepository, times(1)).save(any(Article.class));
  }

  @Test
  @DisplayName("한경 기사 개수 조회 성공 테스트")
  void getAllHanKyungArticlesCount_SuccessTest() {
    // Given
    given(articleRepository.countBySource("HanKyung")).willReturn(5);

    // When
    int count = hankyungCollector.getAllHanKyungArticlesCount();

    // Then
    assertThat(count).isEqualTo(5);
    verify(articleRepository).countBySource("HanKyung"); // 메서드가 호출되었는지 검증
  }

  @Test
  @DisplayName("getAllRss: 정상 RSS XML 파싱 → RssItem 리스트 반환")
  void getAllRss_success() throws Exception {
    // given: 더미 RSS XML
    String rssXml = """
            <rss version="2.0">
              <channel>
                <title>한국경제 전체 뉴스</title>
                <item>
                  <title><![CDATA[첫 번째 기사 제목]]></title>
                  <link>https://www.hankyung.com/politics/2025/09/11/ABC123/</link>
                  <pubDate>Wed, 17 Sep 2025 09:30:00 +0900</pubDate>
                </item>
                <item>
                  <title><![CDATA[두 번째 기사 제목]]></title>
                  <link>https://www.hankyung.com/economy/2025/09/11/DEF456/</link>
                  <pubDate>Wed, 17 Sep 2025 10:05:30 +0900</pubDate>
                </item>
              </channel>
            </rss>
            """;

    // Jsoup 정적 메서드 Mock
    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      Connection conn = mock(Connection.class);
      Connection.Response resp = mock(Connection.Response.class);

      // connect() 동작만 mock
      jsoupMock.when(() -> Jsoup.connect("https://www.hankyung.com/feed/all-news"))
          .thenReturn(conn);
      when(conn.userAgent("Mozilla/5.0 (MonewBot)")).thenReturn(conn);
      when(conn.timeout(10000)).thenReturn(conn);
      when(conn.ignoreContentType(true)).thenReturn(conn);
      when(conn.execute()).thenReturn(resp);
      when(resp.body()).thenReturn(rssXml);

      HankyungCollector sut = new HankyungCollector(webClient, articleRepository, interestRepository, keywordRepository);

      // when
      List<RssItem> items = sut.getAllRss();

      // then
      assertThat(items).hasSize(2);

      RssItem first = items.get(0);
      assertThat(first.title()).isEqualTo("첫 번째 기사 제목");
      assertThat(first.link()).isEqualTo("https://www.hankyung.com/politics/2025/09/11/ABC123/");
      assertThat(first.publishedAt()).isEqualTo(LocalDateTime.of(2025, 9, 17, 9, 30));

      RssItem second = items.get(1);
      assertThat(second.title()).isEqualTo("두 번째 기사 제목");
      assertThat(second.link()).isEqualTo("https://www.hankyung.com/economy/2025/09/11/DEF456/");
      assertThat(second.publishedAt()).isEqualTo(LocalDateTime.of(2025, 9, 17, 10, 5, 30));

      // 호출 검증
      jsoupMock.verify(() -> Jsoup.connect("https://www.hankyung.com/feed/all-news"));
      verify(conn).userAgent("Mozilla/5.0 (MonewBot)");
      verify(conn).timeout(10000);
      verify(conn).ignoreContentType(true);
      verify(conn).execute();
      verify(resp).body();
    }
  }

  @Test
  @DisplayName("getAllRss: Jsoup 예외 시 예외 전파")
  void getAllRss_throws_whenJsoupFails() throws IOException {
    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      Connection conn = mock(Connection.class);

      jsoupMock.when(() -> Jsoup.connect("https://www.hankyung.com/feed/all-news"))
          .thenReturn(conn);
      when(conn.userAgent(any())).thenReturn(conn);
      when(conn.timeout(anyInt())).thenReturn(conn);
      when(conn.ignoreContentType(anyBoolean())).thenReturn(conn);
      // execute에서 예외
      when(conn.execute()).thenThrow(new RuntimeException("network fail"));

      HankyungCollector sut = new HankyungCollector(null, null, null, null);

      assertThatThrownBy(sut::getAllRss)
          .isInstanceOf(Exception.class) // 메서드 시그니처가 throws Exception 이므로
          .hasMessageContaining("network fail");
    }
  }

  @Test
  @DisplayName("getArticleSummary: div.summary 태그의 HTML을 정상적으로 반환")
  void getArticleSummary_success() throws Exception {
    // given
    String articleUrl = "https://www.hankyung.com/politics/2025/09/11/ABC123/";

    // 테스트용 HTML: 개행 및 공백을 일부러 넣어서 후처리 확인
    String html = """
            <html>
              <body>
                <div class="summary">
                  <p>첫 번째 줄</p>
                  <p>두 번째 줄</p>
                </div>
              </body>
            </html>
            """;

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      Connection conn = mock(Connection.class);

      // connect만 mock
      jsoupMock.when(() -> Jsoup.connect(articleUrl)).thenReturn(conn);
      when(conn.userAgent(anyString())).thenReturn(conn);
      when(conn.header(eq("Accept-Language"), anyString())).thenReturn(conn);
      when(conn.timeout(anyInt())).thenReturn(conn);

      // .get() 호출 시 실제 Jsoup.parse로 만든 Document 반환
      Document realDoc = Jsoup.parse(html);
      when(conn.get()).thenReturn(realDoc);

      HankyungCollector sut = new HankyungCollector(null, null, null, null);

      // when
      String result = sut.getArticleSummary(articleUrl);

      // then
      assertThat(result)
          .isEqualTo("<p>첫 번째 줄</p><p>두 번째 줄</p>");

      // 호출 검증
      jsoupMock.verify(() -> Jsoup.connect(articleUrl));
      verify(conn).userAgent(anyString());
      verify(conn).header(eq("Accept-Language"), anyString());
      verify(conn).timeout(10000);
      verify(conn).get();
    }
  }

  @Test
  @DisplayName("getArticleSummary: div.summary 태그가 없을 경우 빈 문자열 반환")
  void getArticleSummary_emptyWhenNoSummaryDiv() throws Exception {
    // given
    String articleUrl = "https://www.hankyung.com/no-summary";

    String html = """
            <html>
              <body>
                <div class="content">본문 내용</div>
              </body>
            </html>
            """;

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      Connection conn = mock(Connection.class);

      jsoupMock.when(() -> Jsoup.connect(articleUrl)).thenReturn(conn);
      when(conn.userAgent(anyString())).thenReturn(conn);
      when(conn.header(eq("Accept-Language"), anyString())).thenReturn(conn);
      when(conn.timeout(anyInt())).thenReturn(conn);

      Document realDoc = Jsoup.parse(html);
      when(conn.get()).thenReturn(realDoc);

      HankyungCollector sut = new HankyungCollector(null, null, null, null);

      // when
      String result = sut.getArticleSummary(articleUrl);

      // then
      assertThat(result).isEmpty();
    }
  }
}
