package com.codeit.monew.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.mapper.InterestMapper;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import com.codeit.monew.interest.request.InterestRegisterRequest;
import com.codeit.monew.interest.response_dto.CursorPageResponseInterestDto;
import com.codeit.monew.interest.response_dto.InterestDto;
import com.codeit.monew.subscriptions.repository.SubscriptionRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
@DisplayName("InterestService 테스트")
class InterestServiceTest {

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private KeywordRepository keywordRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private InterestMapper interestMapper;

  @InjectMocks
  private InterestService interestService;

  private InterestRegisterRequest request;
  private InterestDto expectedDto;
  private User mockUser;

  @BeforeEach
  void setUp() {
    // Given - 요청 데이터 준비
    request = new InterestRegisterRequest(
        "프로그래밍",
        Arrays.asList("자바", "스프링", "개발")
    );

    mockUser = User.builder()
        .id(UUID.randomUUID())
        .email("test@test.com")
        .build();

    // Given - 예상 응답 데이터 준비
    expectedDto = new InterestDto(
        UUID.randomUUID(),
        "프로그래밍",
        Arrays.asList("자바", "스프링", "개발"),
        0L,
        false
    );
  }

  @Test
  @DisplayName("관심사 등록 요청시 정상적으로 응답 DTO를 반환한다")
  void registerInterest_Success() {
    // Given
    given(interestRepository.findAllByIsDeletedFalse()).willReturn(List.of());

    Interest mockInterest = Interest.builder().build();
    given(interestRepository.save(any(Interest.class))).willReturn(mockInterest);

    List<Keyword> mockKeywords = List.of();
    given(keywordRepository.saveAll(anyList())).willReturn(mockKeywords);

    given(interestMapper.toDto(any(Interest.class), anyList(), anyBoolean()))
        .willReturn(expectedDto);

    // When
    InterestDto result = interestService.registerInterest(request);

    // Then
    assertThat(result)
        .isNotNull()
        .isEqualTo(expectedDto);
  }

  @Test
  @DisplayName("기본 조건으로 관심사 목록을 조회하면 기대한 DTO를 반환한다")
  void searchInterests_ReturnsExpectedDto_DefaultCondition() {
    // Given
    String keyword = null;
    String orderBy = "createdAt";
    String direction = "DESC";
    String cursor = "3";
    LocalDateTime after = null;
    int limit = 2;
    UUID requestUserId = UUID.randomUUID();

    given(userRepository.findById(requestUserId)).willReturn(Optional.of(mockUser));

    Interest mockInterest = createMockInterest();
    List<Interest> mockInterests = new ArrayList<>(Arrays.asList(mockInterest, mockInterest, mockInterest));

    given(interestRepository.findInterestsWithCursor(keyword, orderBy, direction, cursor, after, limit))
        .willReturn(mockInterests);

    given(keywordRepository.findAllByInterest_IdAndDeletedAtFalse(any()))
        .willReturn(List.of());

    given(subscriptionRepository.existsByUserAndInterest(mockUser, mockInterest)).willReturn(false);


    InterestDto expectedDto = createMockDto(); // 간단한 mock
    given(interestMapper.toDto(any(), anyList(), eq(false)))
        .willReturn(expectedDto);

    given(interestRepository.count()).willReturn(3L);

    // When
    CursorPageResponseInterestDto result = interestService.searchInterests(
        keyword, orderBy, direction, cursor, after, limit, requestUserId
    );

    // Then
    assertThat(result).isNotNull();
    assertThat(result.content()).containsOnly(expectedDto);
  }

  private InterestDto createMockDto() {
    return new InterestDto(
        UUID.randomUUID(),
        "mock",
        List.of("키워드1", "키워드2"),
        100L,
        false
    );
  }

  private Interest createMockInterest() {
    return Interest.builder()
        .id(UUID.randomUUID())
        .name("mock-interest")
        .subscriberCount(100)
        .createdAt(LocalDateTime.now())
        .isDeleted(false)
        .build();
  }
}
