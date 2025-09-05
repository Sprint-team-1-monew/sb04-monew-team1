package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.exception.article.ArticleNotFoundException;
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
// 서비스 클래스명을 실제 코드에 맞게 바꿔주세요 (예: ArticleService)
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock
  ArticleRepository articleRepository;

  @InjectMocks
  ArticleService service; // 실제 서비스 클래스명으로 변경

  @Test
  @DisplayName("deleteArticle: 존재하는 기사면 deleted=true 로 설정 후 저장한다")
  void deleteArticle_marksDeleted_andSaves() {
    // Given
    UUID id = UUID.randomUUID();
    Article article = Article.builder()
        .deleted(false)
        .build();
    given(articleRepository.findById(id)).willReturn(Optional.of(article));

    // When
    service.deleteArticle(id);

    // Then
    assertThat(article.isDeleted()).isTrue();
    then(articleRepository).should().findById(id);
    then(articleRepository).should().save(article);
  }

  @Test
  @DisplayName("deleteArticle: 기사가 없으면 ArticleNotFoundException을 던지고 save는 호출되지 않는다")
  void deleteArticle_throws_whenNotFound_andNoSave() {
    // Given
    UUID id = UUID.randomUUID();
    given(articleRepository.findById(id)).willReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> service.deleteArticle(id))
        .isInstanceOf(ArticleNotFoundException.class);

    then(articleRepository).should().findById(id);
    then(articleRepository).should(never()).save(org.mockito.Mockito.any());
  }
}