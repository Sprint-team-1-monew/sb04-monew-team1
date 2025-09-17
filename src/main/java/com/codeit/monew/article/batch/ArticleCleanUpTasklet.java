package com.codeit.monew.article.batch;


import com.codeit.monew.article.naver.NaverNewsCollector;
import com.codeit.monew.article.rss.ChoSunCollector;
import com.codeit.monew.article.rss.HankyungCollector;
import com.codeit.monew.notification.event.ArticleCreatedEvent;
import com.codeit.monew.subscriptions.repository.SubscriptionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCleanUpTasklet implements Tasklet {

  private final NaverNewsCollector naverNewsCollector;
  private final ChoSunCollector choSunCollector;
  private final HankyungCollector hanKyungCollector;

  private final ApplicationEventPublisher publisher;
  private final SubscriptionRepository subscriptionRepository;

  private final MeterRegistry meterRegistry;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {

    int newNaverArticleCounts = 0;
    int newChoSunArticleCounts = 0;
    int newHanKyungArticleCounts = 0;

    Map<UUID, Integer> naverCounts = new HashMap<>();
    Map<UUID, Integer> choSunCounts = new HashMap<>();
    Map<UUID, Integer> hanKyungCounts = new HashMap<>();
    Map<UUID, Integer> allCounts = new HashMap<>();

    try {
      naverCounts = naverNewsCollector.naverArticleCollect();
      newNaverArticleCounts = naverCounts.values()
          .stream()
          .mapToInt(Integer::intValue)
          .sum();
    } catch (Exception e) {
      log.error("네이버 기사 수집 실패", e);
    }

    try {
      choSunCounts = choSunCollector.chousunArticleCollect();
      newChoSunArticleCounts = choSunCounts.values()
          .stream()
          .mapToInt(Integer::intValue)
          .sum();
    } catch (Exception e) {
      log.error("조선 기사 수집 실패", e);
    }

    try {
      hanKyungCounts = hanKyungCollector.hanKyungArticleCollect();
      newHanKyungArticleCounts = hanKyungCounts.values()
          .stream()
          .mapToInt(Integer::intValue)
          .sum();
    } catch (Exception e) {
      log.error("한경 기사 수집 실패", e);
    }

    int allNaverArticleCounts = 0;
    int allChoSunArticleCounts = 0;
    int allHanKyungArticleCounts = 0;

    try {
      allNaverArticleCounts = naverNewsCollector.getAllNaverArticlesCount();
    } catch (Exception e) {
      log.error("네이버 전체 기사 수 조회 실패", e);
    }

    try {
      allChoSunArticleCounts = choSunCollector.getAllChoSunArticlesCount();
    } catch (Exception e) {
      log.error("조선 전체 기사 수 조회 실패", e);
    }

    try {
      allHanKyungArticleCounts = hanKyungCollector.getAllHanKyungArticlesCount();
    } catch (Exception e) {
      log.error("한경 전체 기사 수 조회 실패", e);
    }

    log.info("새롭게 수집된 기사 수: 네이버: {}, 조선: {}, 한경: {}",
        newNaverArticleCounts, newChoSunArticleCounts, newHanKyungArticleCounts);
    log.info("총 기사 수: 네이버: {}, 조선: {}, 한경: {}",
        allNaverArticleCounts, allChoSunArticleCounts, allHanKyungArticleCounts);


    meterRegistry.counter("article.collect.count", "source", "NAVER")
        .increment(newNaverArticleCounts);

    meterRegistry.counter("article.collect.count", "source", "CHOSUN")
        .increment(newChoSunArticleCounts);

    meterRegistry.counter("article.collect.count", "source", "HANKYUNG")
        .increment(newHanKyungArticleCounts);

    // 알림 관리
    Stream.of(naverCounts, choSunCounts, hanKyungCounts)
            .forEach(map -> map.forEach(
                (interestId, count) -> allCounts.merge(interestId, count, Integer::sum)
            ));

    // 알림 이벤트 발행
    allCounts.forEach((interestId, count) -> {
      subscriptionRepository.findAllByInterestId(interestId)
          .forEach(sub -> publisher.publishEvent(new ArticleCreatedEvent(
              sub.getUser().getId(),
              interestId,
              sub.getInterest().getName(),
              count
          )));
    });

    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}
