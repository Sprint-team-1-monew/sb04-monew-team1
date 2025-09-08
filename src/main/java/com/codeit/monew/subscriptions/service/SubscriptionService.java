package com.codeit.monew.subscriptions.service;

import com.codeit.monew.exception.interest.InterestErrorCode;
import com.codeit.monew.exception.interest.InterestException;
import com.codeit.monew.exception.subscription.SubscriptionErrorCode;
import com.codeit.monew.exception.subscription.SubscriptionException;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import com.codeit.monew.subscriptions.dto.SubscriptionDto;
import com.codeit.monew.subscriptions.entity.Subscription;
import com.codeit.monew.subscriptions.mapper.SubscriptionMapper;
import com.codeit.monew.subscriptions.repository.SubscriptionRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final KeywordRepository keywordRepository;

  private final SubscriptionMapper subscriptionMapper;

  public SubscriptionDto subscribe(UUID userId, UUID interestId) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));

    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new InterestException(
            InterestErrorCode.INTEREST_NOT_FOUND, Map.of("interestId", interestId)));

    if (subscriptionRepository.existsByUserAndInterest(user, interest)) {
      throw new SubscriptionException(SubscriptionErrorCode.ALREADY_SUBSCRIBED,
          Map.of("userId", user.getId(), "interestId", interest.getId())
      );
    }

    Subscription subscription = Subscription.builder()
        .user(user)
        .interest(interest)
        .build();

    Subscription saved = subscriptionRepository.save(subscription);

    // 구독자 수 증가
    interest.increaseSubscriber();

    // 키워드 직접 조회
    List<Keyword> keywords = keywordRepository.findByInterestAndDeletedAtFalse(interest);

    return subscriptionMapper.toDto(saved, keywords);
  }
}
