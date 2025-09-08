package com.codeit.monew.article.naver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient(WebClient.Builder builder, NaverApiProperties properties) {
    return builder
        .baseUrl("https://openapi.naver.com")
        .defaultHeader("X-Naver-Client-Id", properties.getClientId())
        .defaultHeader("X-Naver-Client-Secret", properties.getClientSecret())
        .build();
  }
}
