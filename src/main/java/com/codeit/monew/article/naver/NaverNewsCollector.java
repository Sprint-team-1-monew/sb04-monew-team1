package com.codeit.monew.article.naver;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverNewsCollector {

  private final WebClient webClient;
  private final ArticleRepository articleRepository;
  private final InterestRepository interestRepository;
  private final KeywordRepository keywordRepository;

  public NaverNewsResponse searchNews(String interestName, Integer display, Integer start, String sort) {
    log.info("네이버 API 요청 : {}", interestName);
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/v1/search/news.json")
            .queryParam("query", interestName)
            .queryParam("display", display)
            .queryParam("start", start)
            .queryParam("sort", sort)
            .build())
        .retrieve()
        // 429면 에러로 전달 (Retry에서 잡게)
        .onStatus(s -> s.value() == 429, resp -> resp.createException().flatMap(Mono::error))
        .bodyToMono(NaverNewsResponse.class)

        // 429만 지수백오프로 최대 3회 재시도 (2s, 4s, 8s)
        .retryWhen(
            Retry.backoff(3, Duration.ofSeconds(2))
                .filter(ex -> ex instanceof WebClientResponseException w &&
                    w.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
                .transientErrors(true)
        )
        .block();
  }

  @Transactional
  //@Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")// 매 시간 정각 마다
  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")// 매 분 마다
  public void fetchAndSaveHourly() {
    log.info("네이버 기사 수집 스케줄링 수집 시작: {}", LocalDateTime.now());
    int display = 10;
    int start = 1;
    String sort = "date";

    List<Interest> interests = interestRepository.findAll();
    // 모든 관심사를 가져옴

    for (Interest interest : interests) {
      Optional<Article> lastArticle = articleRepository.findTop1ByInterestOrderByArticlePublishDateDesc(interest);
      while(true){
        NaverNewsResponse naverNewsResponse = searchNews(interest.getName(), display, start, sort);
        for (NaverNewsItem naverNewsItem : naverNewsResponse.items()) {
          Article article = buildArticleFromNaverItem(naverNewsItem, interest);
          if(isArticleDuplicated(article)){
            continue; // 중복 되었으니 해당 아이템을 넘기고 다음 아이템으로
          }
          articleRepository.save(article);
        }
        LocalDateTime itemPubDate = convertToLocalDateTime(naverNewsResponse.items().get(display-1).pubDate());
        if (lastArticle.isPresent()){
          if (itemPubDate.isAfter(lastArticle.get().getArticlePublishDate())) {
            break; // 오른쪽(마지막 Article)이 더 최신이면 루프 종료
          }
        }

        //다음 페이지로 (네이버는 start를 display만큼 더하기)
        start += display;
        if (start > 1000) break; // 네이버 최대 start 보호
      }
    }
    log.info("네이버 기사 수집 스케줄링 수집 완료: {}", LocalDateTime.now());
  }

  @Transactional
  protected Article buildArticleFromNaverItem(NaverNewsItem item, Interest interest) {

    List<Keyword> keywords = keywordRepository.findByInterest(interest);

    String description = item.description();

    for (Keyword keyword : keywords) {
      String word = keyword.getKeyword();
      if (word != null && !word.isBlank()) {
        description = description.replaceAll("(?i)" + Pattern.quote(word),
            "<b>" + word + "</b>");
      }
    }

    return Article.builder()
        .source("Naver")
        .sourceUrl(item.originallink())             // 뉴스 원문 URL
        .articleTitle(item.title())                 // 뉴스 제목
        .articlePublishDate(convertToLocalDateTime(item.pubDate())) // pubDate 문자열 → LocalDateTime
        .articleSummary(description)
        .articleCommentCount(0)
        .articleViewCount(0)
        .deleted(false)
        .interest(interest)
        .build();
  }

  private static final DateTimeFormatter NAVER_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);


  private LocalDateTime convertToLocalDateTime(String pubDate) {
    return OffsetDateTime.parse(pubDate, NAVER_DATE_FORMATTER).toLocalDateTime();
  }

  @Transactional
  protected boolean isArticleDuplicated(Article article) {
    log.info("네이버 기사 중복 검사 시작: {}", article.getSourceUrl());
    Optional<Article> articleOptional = articleRepository.findBySourceUrl(article.getSourceUrl()); // 없어야 됨
    if (articleOptional.isPresent()) {
      log.info("네이버 기사 중복 검사 발견: {}", article.getSourceUrl());
      return true;
      /*Map<String, Object> details = new HashMap<>();
      details.put("이유: ", "기사가 중복 됨");
      throw new ArticleDuplicateException(details);*/
    } 
    log.info("네이버 기사 중복 검사 종료: {}", article.getSourceUrl());
    return false;
  }
}
