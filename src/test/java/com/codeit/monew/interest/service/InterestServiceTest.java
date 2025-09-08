package com.codeit.monew.interest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.mapper.InterestMapper;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import com.codeit.monew.interest.request.InterestRegisterRequest;
import com.codeit.monew.interest.response_dto.InterestDto;
import java.util.Arrays;
import java.util.List;
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
  private InterestMapper interestMapper;

  @InjectMocks
  private InterestService interestService;

  private InterestRegisterRequest request;
  private InterestDto expectedDto;

  @BeforeEach
  void setUp() {
    // Given - 요청 데이터 준비
    request = new InterestRegisterRequest(
        "프로그래밍",
        Arrays.asList("자바", "스프링", "개발")
    );

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
    given(interestRepository.findAllByDeletedAtFalse()).willReturn(List.of());

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
}
