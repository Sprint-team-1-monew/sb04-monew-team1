package com.codeit.monew.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.article.naver.NaverNewsItem;
import com.codeit.monew.article.naver.NaverNewsResponse;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.service.NaverNewsService;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class NaverNewsServiceTest {

  @Mock WebClient webClient; // Spy에서 searchNews를 스텁하므로 체인 목킹은 불필요
  @Mock ArticleRepository articleRepository;

  NaverNewsService naverNewsService;

  @Test
  void 뉴스_기사의_엔티티화_성공_테스트() {
    int display = 10;
    int start = 1;
    String sort = "date";
  }
}