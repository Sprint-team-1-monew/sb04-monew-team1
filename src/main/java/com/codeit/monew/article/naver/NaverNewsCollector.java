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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    log.info("네이버 API 요청 관심사: {}, 표시 기사: {}, 시작 기사: {}, 정렬: {}", interestName, display, start, sort);
    try {
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
          // 429만 재시도
          .retryWhen(
              Retry.backoff(3, Duration.ofSeconds(2))
                  .filter(ex -> ex instanceof WebClientResponseException w &&
                      w.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
                  .transientErrors(true)
          )
          .block();
    } catch (WebClientResponseException e) {
      log.error("Naver API 오류 status={}, body={}",
          e.getRawStatusCode(), e.getResponseBodyAsString());
      throw e;
    }
  }

  @Transactional
  @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
  public void fetchAndSaveHourly() throws InterruptedException {
    log.info("네이버 기사 수집 스케줄링 수집 시작: {}", LocalDateTime.now());
    int display = 100;
    int start = 1;
    String sort = "date";

    List<Interest> interests = interestRepository.findAll();
    // 모든 관심사를 가져옴

    for (Interest interest : interests) {
      Optional<Article> lastArticle = articleRepository.findTop1ByInterestOrderByArticlePublishDateDesc(interest);
      boolean isPrior = false;
      List<Article> sortedNaverNewsItems = new ArrayList<>();
      while(start <= 1000){
        NaverNewsResponse naverNewsResponse = searchNews(interest.getName(), display, start, sort);
        for(NaverNewsItem naverNewsItem : naverNewsResponse.items()) {
          Article newArticle = buildArticleFromNaverItem(naverNewsItem, interest);
          if(isArticleDuplicated(newArticle)){
            continue; // 중복 되었으니 해당 아이템을 넘기고 다음 아이템으로
          }

          if (lastArticle.isPresent()){
            if (newArticle.getArticlePublishDate().isBefore(lastArticle.get().getArticlePublishDate())) {
              log.info("네이버 기사 발행일: {}, 저장된 마지막 기사 발행일: {}", newArticle.getArticlePublishDate(), lastArticle.get().getArticlePublishDate());
              isPrior = true;
              break;
            }
          }
          sortedNaverNewsItems.add(newArticle);
        }
        //다음 페이지로 (네이버는 start를 display만큼 더하기)
        if (isPrior) break;
        start += display;
      }

      for (int i = sortedNaverNewsItems.size() - 1; i >= 0; i--) {
        articleRepository.save(sortedNaverNewsItems.get(i));
        TimeUnit.MILLISECONDS.sleep(1); // createdAt 값이 유니크해져서 정렬하기 편해진다.
      }
    }
    log.info("네이버 기사 수집 스케줄링 수집 완료: {}", LocalDateTime.now());
    log.info("네이버 기사 수집 후 총 기사 개수: {}", articleRepository.count());
  }
  // 관심사가 등록될 때 호출 할까?
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
    } 
    log.info("네이버 기사 중복 검사 종료: {}", article.getSourceUrl());
    return false;
  }
}
