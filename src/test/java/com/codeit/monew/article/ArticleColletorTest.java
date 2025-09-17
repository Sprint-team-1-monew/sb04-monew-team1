package com.codeit.monew.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class ArticleColletorTest {

  @Mock
  ArticleRepository articleRepository;

  @Mock
  InterestRepository interestRepository;

  @Mock
  KeywordRepository keywordRepository;

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
}
