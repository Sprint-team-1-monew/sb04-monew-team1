package com.codeit.monew.article.naver;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "naver.api")
public class NaverApiProperties {
  private String clientId;
  private String clientSecret;
}
