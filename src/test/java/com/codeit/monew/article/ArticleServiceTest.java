package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.exception.article.ArticleNotFoundException;
import com.codeit.monew.interest.entity.Interest;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock
  ArticleRepository articleRepository;

  @InjectMocks
  ArticleService service; // 실제 서비스 클래스명으로 변경

  @Test
  @DisplayName("기사 논리 삭제")
  void softDeleteArticle_marksDeleted_andSaves() {
    // Given
    UUID id = UUID.randomUUID();
    Article article = Article.builder()
        .deleted(false)
        .build();
    given(articleRepository.findById(id)).willReturn(Optional.of(article));

    // When
    service.softDeleteArticle(id);

    // Then
    assertThat(article.isDeleted()).isTrue();
    then(articleRepository).should().findById(id);
    then(articleRepository).should().save(article);
  }

  @Test
  @DisplayName("기사 물리 삭제")
  void hardDeleteArticle_marksDeleted_andSaves() {
    // Given
    UUID id = UUID.randomUUID();
    Interest interest = Interest.builder().name("test").subscriberCount(0).build();
    Article article = Article.builder()
        .source("NAVER")
        .sourceUrl("https://example.com")
        .articleTitle("네이버 뉴스 제목")
        .articlePublishDate(LocalDateTime.now())
        .articleSummary("뉴스 요약")
        .articleCommentCount(0L)
        .articleViewCount(0L)
        .deleted(false)
        .interest(interest)
        .build();

    given(articleRepository.save(any(Article.class)))
        .willAnswer(inv -> inv.getArgument(0));

    given(articleRepository.findById(id))
        .willReturn(Optional.of(article));
    Article saved = articleRepository.save(article);

    // When
    service.hardDeleteArticle(id);

    // Then: 삭제 로직이 delete(...)를 호출했는지 검증
    then(articleRepository).should().delete(article);
    then(articleRepository).should().findById(id);
    then(articleRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("deleteArticle: 기사가 없으면 ArticleNotFoundException을 던지고 save는 호출되지 않는다")
  void deleteArticle_throws_whenNotFound_andNoSave() {
    // Given
    UUID id = UUID.randomUUID();
    given(articleRepository.findById(id)).willReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> service.softDeleteArticle(id))
        .isInstanceOf(ArticleNotFoundException.class);

    then(articleRepository).should().findById(id);
    then(articleRepository).should(never()).save(org.mockito.Mockito.any());
  }
}