package com.codeit.monew.article.rss;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HankyungCollector {

  private final WebClient webClient;
  private final ArticleRepository articleRepository;
  private final InterestRepository interestRepository;
  private final KeywordRepository keywordRepository;

  @Transactional
  //@Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")// 매 시간 정각 마다
  @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")// 매 분 마다
  public void hankyungArticleFetchAndSaveHourly() throws Exception {
    log.info("한국경제 기사 수집 스케줄링 수집 시작: {}", LocalDateTime.now());
    List<Interest> interests = interestRepository.findAll();

      List<RssItem> rssItems = getAllRss();
    for (int i = rssItems.size() - 1; i >= 0; i--) {
      RssItem rssItem = rssItems.get(i);
      String summary = getArticleSummary(rssItem.link());
      Interest targetInterest = null;

      for (Interest interest : interests) {
        if (rssItem.title().contains(interest.getName())) {
          targetInterest = interest;
          break;
        }
        if (summary.contains(interest.getName())) {
          targetInterest = interest;
          break;
        }
      }

      if (targetInterest == null) {
        continue;
      }

      Article article = buildArticleFromHanKyungItem(rssItem, summary, targetInterest);
      if (!isArticleDuplicated(article)) {
        Article savedArticle = articleRepository.save(article);
        log.info("기사 명: {}, 기사 요약: {}, 발매일: {}, 저장일: {}", savedArticle.getArticleTitle(), savedArticle.getArticleSummary(), savedArticle.getArticlePublishDate(), savedArticle.getCreatedAt());
      }
    }
    log.info("한국경제 기사 수집 스케줄링 수집 종료: {}", LocalDateTime.now());
  }

  private boolean isArticleDuplicated(Article article) {
    log.info("한국경제 기사 중복 검사 시작: {}", article.getSourceUrl());
    Optional<Article> articleOptional = articleRepository.findBySourceUrl(article.getSourceUrl()); // 없어야 됨
    if (articleOptional.isPresent()) {
      log.info("한국경제 기사 중복 검사 발견: {}", article.getSourceUrl());
      return true;
    }
    log.info("한국경제 기사 중복 검사 종료: {}", article.getSourceUrl());
    return false;
  }

  public String getArticleSummary(String articleUrl) throws Exception {
    Document doc = Jsoup.connect(articleUrl)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/126.0 Safari/537.36")
        .header("Accept-Language", "ko,en;q=0.8")
        .timeout(10000)
        .get();

    Element sum = doc.selectFirst("div.summary");
    String summaryInnerHtml = (sum != null) ? sum.html() : "";
    summaryInnerHtml = summaryInnerHtml
        .replaceAll("\\R", "")        // 개행 제거
        .replaceAll(">\\s+<", "><");  // 태그 사이 공백 압축

    return summaryInnerHtml;
  }

  public List<RssItem> getAllRss() throws Exception {
    String xml = Jsoup.connect("https://www.hankyung.com/feed/all-news")
        .userAgent("Mozilla/5.0 (MonewBot)")
        .timeout(10000)
        .ignoreContentType(true)     // XML 응답
        .execute()
        .body();

    Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
    List<RssItem> items = new ArrayList<>();
    for (Element item : doc.select("channel > item")) {
      String title = text(item, "title"); // CDATA 자동 해제됨
      String link  = text(item, "link");
      String pub   = text(item, "pubDate");

      LocalDateTime publishedAt = parsePubDate(pub); // 아래 헬퍼

      items.add(new RssItem(title, link, publishedAt));
    }
    return items;
  }

  private static String text(Element parent, String tag) {
    Element el = parent.selectFirst(tag);
    return el == null ? "" : el.text().trim();
  }

  private static final List<DateTimeFormatter> PUB_FORMATS = List.of(
      DateTimeFormatter.RFC_1123_DATE_TIME,
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
  );

  private static LocalDateTime parsePubDate(String s) {
    for (DateTimeFormatter f : PUB_FORMATS) {
      try {
        return ZonedDateTime.parse(s, f)
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime();
      } catch (Exception ignored) {}
    }
    return null;
  }

  private static final DateTimeFormatter NAVER_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

  private Article buildArticleFromHanKyungItem(RssItem rssItem, String summary, Interest interest) {

    List<Keyword> keywords = keywordRepository.findByInterest(interest);

    for (Keyword keyword : keywords) {
      String word = keyword.getKeyword();
      if (word != null && !word.isBlank()) {
        summary = summary.replaceAll("(?i)" + Pattern.quote(word),
            "<b>" + word + "</b>");
      }
    }

    return Article.builder()
        .source("HanKyung")
        .sourceUrl(rssItem.link())             // 뉴스 원문 URL
        .articleTitle(rssItem.title())                 // 뉴스 제목
        .articlePublishDate(rssItem.publishedAt()) // pubDate 문자열 → LocalDateTime
        .articleSummary(summary)
        .articleCommentCount(0)
        .articleViewCount(0)
        .deleted(false)
        .interest(interest)
        .build();
  }

}
