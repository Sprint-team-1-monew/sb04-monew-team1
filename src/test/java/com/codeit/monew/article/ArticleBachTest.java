package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.article.batch.ArticleCleanUpTasklet;
import com.codeit.monew.article.naver.NaverNewsCollector;
import com.codeit.monew.article.rss.ChoSunCollector;
import com.codeit.monew.article.rss.HankyungCollector;
import com.codeit.monew.subscriptions.repository.SubscriptionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class ArticleBachTest {

  @Mock private NaverNewsCollector naverNewsCollector;
  @Mock private ChoSunCollector choSunCollector;
  @Mock private HankyungCollector hanKyungCollector;
  @Mock private MeterRegistry meterRegistry;
  @Mock private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private ArticleCleanUpTasklet articleCleanUpTasklet;

  @Test
  @DisplayName("Tasklet 실행 시 각 Collector 호출, Counter increment, ExitStatus=COMPLETED")
  void execute_shouldCallCollectorsAndIncrementCounters_andExitCompleted() throws Exception {
    // given
    StepContribution contribution = mock(StepContribution.class);
    ChunkContext chunkContext = mock(ChunkContext.class);

    int newNaverArticleCounts = 5;
    int newChoSunArticleCounts = 5;
    int newHanKyungArticleCounts = 5;

    // 각 컬렉터가 반환할 Map 스텁 (합계가 위 숫자가 되도록)
    Map<UUID, Integer> naverMap    = Map.of(UUID.randomUUID(), 3, UUID.randomUUID(), 2);
    Map<UUID, Integer> choSunMap   = Map.of(UUID.randomUUID(), 5);
    Map<UUID, Integer> hanKyungMap = Map.of(UUID.randomUUID(), 1, UUID.randomUUID(), 4);

    when(naverNewsCollector.naverArticleCollect()).thenReturn(naverMap);
    when(choSunCollector.chousunArticleCollect()).thenReturn(choSunMap);
    when(hanKyungCollector.hanKyungArticleCollect()).thenReturn(hanKyungMap);

    when(naverNewsCollector.getAllNaverArticlesCount()).thenReturn(5);
    when(choSunCollector.getAllChoSunArticlesCount()).thenReturn(5);
    when(hanKyungCollector.getAllHanKyungArticlesCount()).thenReturn(5);

    when(subscriptionRepository.findAllByInterestId(any(UUID.class)))
        .thenReturn(java.util.Collections.emptyList());

    // Counter mock
    Counter naverCounter    = mock(Counter.class);
    Counter choSunCounter   = mock(Counter.class);
    Counter hanKyungCounter = mock(Counter.class);

    when(meterRegistry.counter("article.collect.count", "source", "NAVER")).thenReturn(naverCounter);
    when(meterRegistry.counter("article.collect.count", "source", "CHOSUN")).thenReturn(choSunCounter);
    when(meterRegistry.counter("article.collect.count", "source", "HANKYUNG")).thenReturn(hanKyungCounter);

    // when
    RepeatStatus result = articleCleanUpTasklet.execute(contribution, chunkContext);

    // then
    assertThat(result).isEqualTo(RepeatStatus.FINISHED);
    verify(contribution).setExitStatus(ExitStatus.COMPLETED);

    verify(naverNewsCollector).naverArticleCollect();
    verify(choSunCollector).chousunArticleCollect();
    verify(hanKyungCollector).hanKyungArticleCollect();

    // increment(double) 이므로 double로 검증하면 깔끔
    verify(naverCounter   , times(1)).increment((double) newNaverArticleCounts);
    verify(choSunCounter  , times(1)).increment((double) newChoSunArticleCounts);
    verify(hanKyungCounter, times(1)).increment((double) newHanKyungArticleCounts);

    verify(naverNewsCollector).getAllNaverArticlesCount();
    verify(choSunCollector).getAllChoSunArticlesCount();
    verify(hanKyungCollector).getAllHanKyungArticlesCount();
  }
}
