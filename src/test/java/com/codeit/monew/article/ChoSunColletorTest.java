package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.rss.ChoSunCollector;
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
public class ChoSunColletorTest {

  @Mock
  private WebClient webClient;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private KeywordRepository keywordRepository;

  @InjectMocks
  private ChoSunCollector choSunCollector;

  @Test
  @DisplayName("조선 일보 테스트")
  void chosun_collector_test() throws Exception {
    // given
    ChoSunCollector collector = new ChoSunCollector(
        null, articleRepository, interestRepository, keywordRepository);

    Interest interest = Interest.builder().name("상속세").subscriberCount(0).build();
    when(interestRepository.findAll()).thenReturn(List.of(interest));

    LocalDateTime now = LocalDateTime.now();
    RssItem item = new RssItem(
        "李대통령 “참사는 보수 정권에서 주로 발생” 사실일까?",
        "https://www.chosun.com/national/labor/2025/09/11/RBRFN7M7HFEONCIWMZSVZL7UYE/",
        now
    );

    when(articleRepository.save(any(Article.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    //when
    try (MockedStatic<ChoSunCollector> mocked = mockStatic(ChoSunCollector.class)) {
      mocked.when(ChoSunCollector::getAllRss).thenReturn(List.of(item));
      mocked.when(() -> ChoSunCollector.getArticleSummary(item.link()))
          .thenReturn("요약... 상속세 ...");

      when(articleRepository.findBySourceUrl(item.link())).thenReturn(Optional.empty());

      collector.chousunArticleCollect();

      verify(articleRepository, times(1)).save(argThat(article ->
          article.getSource().equals("ChoSun") &&
              article.getSourceUrl().equals(item.link()) &&
              article.getArticleTitle().equals(item.title()) &&
              article.getArticlePublishDate().equals(now) &&
              article.getInterest() != null &&
              article.getInterest().getName().equals("상속세")
      ));
    }
  }

  @Test
  @DisplayName("조선 기사 개수 조회 성공 테스트")
  void getAllChoSunArticlesCount_SuccessTest() {
    // Given
    given(articleRepository.countBySource("ChoSun")).willReturn(5);

    // When
    int count = choSunCollector.getAllChoSunArticlesCount();

    // Then
    assertThat(count).isEqualTo(5);
    verify(articleRepository).countBySource("ChoSun"); // 메서드가 호출되었는지 검증
  }

  @Test
  @DisplayName("조선 일보 관심사가 매칭 되지 않으면 저장 안함")
  void chosun_not_found_interest_no_save() throws Exception {
    // given
    ChoSunCollector collector = new ChoSunCollector(
        null, articleRepository, interestRepository, keywordRepository
    );

    Interest interest1 =  Interest.builder().name("부동산").subscriberCount(0).build();
    Interest interest2 =  Interest.builder().name("주식").subscriberCount(0).build();

    // 관심사 매칭 안됨
    when(interestRepository.findAll()).thenReturn(List.of(interest1, interest2));
    LocalDateTime now = LocalDateTime.now();
    RssItem item = new RssItem(
        "李대통령 “참사는 보수 정권에서 주로 발생” 사실일까?",
        "https://www.chosun.com/national/labor/2025/09/11/RBRFN7M7HFEONCIWMZSVZL7UYE/",
        now
    );

    try (MockedStatic<ChoSunCollector> mocked = mockStatic(ChoSunCollector.class)) {
      mocked.when(ChoSunCollector::getAllRss).thenReturn(List.of(item));
      mocked.when(() -> ChoSunCollector.getArticleSummary(item.link()))
          .thenReturn("바다"); // 관심사 미포함

      // when
      collector.chousunArticleCollect();

      // then
      verify(articleRepository, never()).save(any(Article.class));
    }
  }

  @Test
  @DisplayName("getAllRss: 정상 RSS XML 파싱 → RssItem 리스트 반환 (Chosun)")
  void getAllRss_success() throws Exception {
    // given
    String rssXml = """
        <rss version="2.0">
          <channel>
            <title>조선일보 전체 뉴스</title>
            <item>
              <title><![CDATA[첫 번째 기사 제목]]></title>
              <link>https://www.chosun.com/politics/2025/09/11/AAA111/</link>
              <pubDate>Wed, 17 Sep 2025 09:30:00 +0900</pubDate>
            </item>
            <item>
              <title><![CDATA[두 번째 기사 제목]]></title>
              <link>https://www.chosun.com/economy/2025/09/11/BBB222/</link>
              <pubDate>Wed, 17 Sep 2025 10:05:30 +0900</pubDate>
            </item>
          </channel>
        </rss>
        """;

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      Connection conn = mock(Connection.class);
      Connection.Response resp = mock(Connection.Response.class);

      jsoupMock.when(() -> Jsoup.connect("https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"))
          .thenReturn(conn);
      org.mockito.Mockito.when(conn.userAgent("Mozilla/5.0 (MonewBot)")).thenReturn(conn);
      org.mockito.Mockito.when(conn.timeout(10000)).thenReturn(conn);
      org.mockito.Mockito.when(conn.ignoreContentType(true)).thenReturn(conn);
      org.mockito.Mockito.when(conn.execute()).thenReturn(resp);
      org.mockito.Mockito.when(resp.body()).thenReturn(rssXml);

      ChoSunCollector sut = new ChoSunCollector(webClient, articleRepository, interestRepository, keywordRepository);

      // when
      List<RssItem> items = sut.getAllRss();

      // then
      assertThat(items).hasSize(2);

      RssItem first = items.get(0);
      assertThat(first.title()).isEqualTo("첫 번째 기사 제목");
      assertThat(first.link()).isEqualTo("https://www.chosun.com/politics/2025/09/11/AAA111/");
      assertThat(first.publishedAt()).isEqualTo(LocalDateTime.of(2025, 9, 17, 9, 30));

      RssItem second = items.get(1);
      assertThat(second.title()).isEqualTo("두 번째 기사 제목");
      assertThat(second.link()).isEqualTo("https://www.chosun.com/economy/2025/09/11/BBB222/");
      assertThat(second.publishedAt()).isEqualTo(LocalDateTime.of(2025, 9, 17, 10, 5, 30));

      jsoupMock.verify(() -> Jsoup.connect("https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"));
      verify(conn).userAgent("Mozilla/5.0 (MonewBot)");
      verify(conn).timeout(10000);
      verify(conn).ignoreContentType(true);
      verify(conn).execute();
      verify(resp).body();
    }
  }

  @Test
  @DisplayName("getAllRss: Jsoup 예외 시 예외 전파 (Chosun)")
  void getAllRss_throws_whenJsoupFails() throws IOException {
    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
      Connection conn = mock(Connection.class);

      jsoupMock.when(() -> Jsoup.connect("https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"))
          .thenReturn(conn);
      org.mockito.Mockito.when(conn.userAgent(anyString())).thenReturn(conn);
      org.mockito.Mockito.when(conn.timeout(anyInt())).thenReturn(conn);
      org.mockito.Mockito.when(conn.ignoreContentType(anyBoolean())).thenReturn(conn);
      org.mockito.Mockito.when(conn.execute()).thenThrow(new RuntimeException("network fail"));

      assertThatThrownBy(ChoSunCollector::getAllRss)
          .isInstanceOf(Exception.class)
          .hasMessageContaining("network fail");
    }
  }

  @Test
  @DisplayName("getArticleSummary: 선택자에 매칭되는 요소 없으면 빈 문자열")
  void getArticleSummary_emptyWhenNoMatch() throws Exception {
    String articleUrl = "https://www.chosun.com/no-summary";
    String html = """
        <html><body>
          <div class="content">본문</div>
        </body></html>
        """;

    try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
      Connection conn = mock(Connection.class);
      jsoupMock.when(() -> Jsoup.connect(articleUrl)).thenReturn(conn);
      org.mockito.Mockito.when(conn.userAgent(anyString())).thenReturn(conn);
      org.mockito.Mockito.when(conn.header(eq("Accept-Language"), anyString())).thenReturn(conn);
      org.mockito.Mockito.when(conn.timeout(anyInt())).thenReturn(conn);

      Document realDoc = Jsoup.parse(html);
      org.mockito.Mockito.when(conn.get()).thenReturn(realDoc);

      ChoSunCollector sut = new ChoSunCollector(null, null, null, null);

      // when
      String result = sut.getArticleSummary(articleUrl);

      // then
      assertThat(result).isEmpty();
    }
  }
}
