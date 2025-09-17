//package com.codeit.monew.article;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//import com.codeit.monew.article.batch.ArticleCleanUpTasklet;
//import com.codeit.monew.article.naver.NaverNewsCollector;
//import com.codeit.monew.article.rss.ChoSunCollector;
//import com.codeit.monew.article.rss.HankyungCollector;
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.batch.core.ExitStatus;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.repeat.RepeatStatus;
//
//@ExtendWith(MockitoExtension.class)
//public class ArticleBachTest {
//
//  @Mock private NaverNewsCollector naverNewsCollector;
//  @Mock private ChoSunCollector choSunCollector;
//  @Mock private HankyungCollector hanKyungCollector;
//  @Mock private MeterRegistry meterRegistry;
//
//  @InjectMocks                                           // Mockito가 위 @Mock들을 주입
//  private ArticleCleanUpTasklet articleCleanUpTasklet;
//
//  @Test
//  @DisplayName("Tasklet 실행 시 각 Collector 호출, Counter increment, ExitStatus=COMPLETED")
//  void execute_shouldCallCollectorsAndIncrementCounters_andExitCompleted() throws Exception {
//    // given
//    StepContribution contribution = mock(StepContribution.class);
//    ChunkContext chunkContext = mock(ChunkContext.class);
//
//    int newNaverArticleCounts = 5;
//    int newChoSunArticleCounts = 5;
//    int newHanKyungArticleCounts = 5;
//
//    when(naverNewsCollector.naverArticleCollect().values().stream()
//        .mapToInt(Integer::intValue)
//        .sum()).thenReturn(newNaverArticleCounts);
//    when(choSunCollector.chousunArticleCollect().values().stream()
//        .mapToInt(Integer::intValue)
//        .sum()).thenReturn(newChoSunArticleCounts);
//    when(hanKyungCollector.hanKyungArticleCollect().values().stream()
//        .mapToInt(Integer::intValue)
//        .sum()).thenReturn(newHanKyungArticleCounts);
//
//    when(naverNewsCollector.getAllNaverArticlesCount()).thenReturn(5);
//    when(choSunCollector.getAllChoSunArticlesCount()).thenReturn(5);
//    when(hanKyungCollector.getAllHanKyungArticlesCount()).thenReturn(5);
//
//    // 소스별 Counter mock (각각 따로)
//    Counter naverCounter    = mock(Counter.class);
//    Counter choSunCounter   = mock(Counter.class);
//    Counter hanKyungCounter = mock(Counter.class);
//
//    when(meterRegistry.counter("article.collect.count", "source", "NAVER"   ))
//        .thenReturn(naverCounter);
//    when(meterRegistry.counter("article.collect.count", "source", "CHOSUN"  ))
//        .thenReturn(choSunCounter);
//    when(meterRegistry.counter("article.collect.count", "source", "HANKYUNG"))
//        .thenReturn(hanKyungCounter);
//
//    // when
//    RepeatStatus result = articleCleanUpTasklet.execute(contribution, chunkContext);
//
//    // then
//    assertThat(result).isEqualTo(RepeatStatus.FINISHED);
//    verify(contribution).setExitStatus(ExitStatus.COMPLETED);
//
//    verify(naverNewsCollector).naverArticleCollect();
//    verify(choSunCollector).chousunArticleCollect();
//    verify(hanKyungCollector).hanKyungArticleCollect();
//
//    verify(naverCounter   , times(1)).increment(newNaverArticleCounts);
//    verify(choSunCounter  , times(1)).increment(newChoSunArticleCounts);
//    verify(hanKyungCounter, times(1)).increment(newHanKyungArticleCounts);
//
//    verify(naverNewsCollector).getAllNaverArticlesCount();
//    verify(choSunCollector).getAllChoSunArticlesCount();
//    verify(hanKyungCollector).getAllHanKyungArticlesCount();
//  }
//}
