package com.codeit.monew.subscriptions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import com.codeit.monew.subscriptions.dto.SubscriptionDto;
import com.codeit.monew.subscriptions.entity.Subscription;
import com.codeit.monew.subscriptions.mapper.SubscriptionMapper;
import com.codeit.monew.subscriptions.repository.SubscriptionRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService 테스트")
class SubscriptionServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private KeywordRepository keywordRepository;

  @Mock
  private SubscriptionMapper subscriptionMapper;

  @InjectMocks
  private SubscriptionService subscriptionService;

  private UUID userId;
  private UUID interestId;
  private User mockUser;
  private Interest mockInterest;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    interestId = UUID.randomUUID();

    mockUser = User.builder()
        .id(userId)
        .email("test@test.com")
        .build();

    mockInterest = Interest.builder()
        .id(interestId)
        .name("프로그래밍")
        .subscriberCount(0)
        .createdAt(LocalDateTime.now())
        .isDeleted(false)
        .build();
  }

  @Test
  @DisplayName("구독 요청시 SubscriptionDto를 정상적으로 반환한다")
  void subscribe_Success() {
    // Given
    given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
    given(interestRepository.findById(interestId)).willReturn(Optional.of(mockInterest));
    given(subscriptionRepository.existsByUserAndInterest(mockUser, mockInterest)).willReturn(false);

    Subscription mockSubscription = Subscription.builder()
        .id(UUID.randomUUID())
        .user(mockUser)
        .interest(mockInterest)
        .createdAt(LocalDateTime.now())
        .build();
    given(subscriptionRepository.save(any(Subscription.class))).willReturn(mockSubscription);

    List<Keyword> mockKeywords = List.of(
        Keyword.builder().id(UUID.randomUUID()).keyword("자바").interest(mockInterest).build(),
        Keyword.builder().id(UUID.randomUUID()).keyword("스프링").interest(mockInterest).build()
    );
    given(keywordRepository.findByInterestAndDeletedAtFalse(mockInterest)).willReturn(mockKeywords);

    // When
    SubscriptionDto result = subscriptionService.subscribe(userId, interestId);

    // Expected DTO를 subscribe() 후 생성
    SubscriptionDto expectedDto = subscriptionMapper.toDto(mockSubscription, mockKeywords);

    // Then
    assertThat(result)
        .usingRecursiveComparison()
        .isEqualTo(expectedDto);
  }
}