package com.codeit.monew.subscriptions.dto;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.subscriptions.entity.Subscription;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionDto {
  private UUID id;
  private UUID interestId;
  private String interestName;
  private List<String> interestKeywords;
  private Long interestSubscriberCount;
  private LocalDateTime createdAt;

  public static SubscriptionDto from(Subscription subscription, List<Keyword> keywords) {
    Interest interest = subscription.getInterest();
    return SubscriptionDto.builder()
        .id(subscription.getId())
        .interestId(interest.getId())
        .interestName(interest.getName())
        .interestKeywords(
            keywords.stream()
                .map(Keyword::getKeyword)
                .collect(Collectors.toList())
        )
        .interestSubscriberCount(Long.valueOf(interest.getSubscriberCount()))
        .createdAt(subscription.getCreatedAt())
        .build();
  }
}
