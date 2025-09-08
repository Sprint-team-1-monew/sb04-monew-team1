package com.codeit.monew.subscriptions.mapper;

import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.subscriptions.dto.SubscriptionDto;
import com.codeit.monew.subscriptions.entity.Subscription;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

  @Mapping(target = "interestId", expression = "java(subscription.getInterest().getId())")
  @Mapping(target = "interestName", expression = "java(subscription.getInterest().getName())")
  @Mapping(target = "interestKeywords", expression = "java(keywords.stream().map(k -> k.getKeyword()).toList())")
  @Mapping(target = "interestSubscriberCount", expression = "java(Long.valueOf(subscription.getInterest().getSubscriberCount()))")
  SubscriptionDto toDto(Subscription subscription, List<Keyword> keywords);

}