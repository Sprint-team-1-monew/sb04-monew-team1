package com.codeit.monew.article.batch;


import com.codeit.monew.article.naver.NaverNewsCollector;
import com.codeit.monew.article.rss.ChoSunCollector;
import com.codeit.monew.article.rss.HankyungCollector;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCleanUpTasklet implements Tasklet {

  private final NaverNewsCollector naverNewsCollector;
  private final ChoSunCollector choSunCollector;
  private final HankyungCollector hanKyungCollector;

  private final MeterRegistry meterRegistry;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {

    int newNaverArticleCounts = 0;
    int newChoSunArticleCounts = 0;
    int newHanKyungArticleCounts = 0;

    try {
      newNaverArticleCounts = naverNewsCollector.naverArticleCollect();
    } catch (Exception e) {
      log.error("네이버 기사 수집 실패", e);
    }

    try {
      newChoSunArticleCounts = choSunCollector.chousunArticleCollect();
    } catch (Exception e) {
      log.error("조선 기사 수집 실패", e);
    }

    try {
      newHanKyungArticleCounts = hanKyungCollector.hankyungArticleCollect();
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

    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}
