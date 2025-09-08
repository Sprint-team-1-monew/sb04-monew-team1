package com.codeit.monew.interest.service;

import static com.codeit.monew.exception.interest.InterestErrorCode.INTEREST_NAME_DUPLICATION;

import com.codeit.monew.exception.interest.InterestException;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import com.codeit.monew.interest.mapper.InterestMapper;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.KeywordRepository;
import com.codeit.monew.interest.request.InterestRegisterRequest;
import com.codeit.monew.interest.response_dto.InterestDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InterestService {

  private final InterestRepository interestRepository;
  private final KeywordRepository keywordRepository;
  private final InterestMapper interestMapper;

  @Transactional
  public InterestDto registerInterest(InterestRegisterRequest request) {
    // 80% 이상 유사한 이름 체크
    validateSimilarNameExists(request.name());

    // Interest 엔티티 생성 및 저장
    Interest interest = Interest.builder()
        .name(request.name())
        .subscriberCount(0)
        .deletedAt(false)
        .build();

    Interest savedInterest = interestRepository.save(interest);

    // Keyword 엔티티들 생성 및 저장
    List<Keyword> keywords = request.keywords().stream()
        .map(keywordStr -> Keyword.builder()
            .keyword(keywordStr)
            .interest(savedInterest)
            .deletedAt(false)
            .build())
            .collect(Collectors.toList());

    keywordRepository.saveAll(keywords);

    // DTO로 변환하여 반환
    return interestMapper.toDto(savedInterest, keywords, false);
  }

//  @Transactional(readOnly = true)
//  public CursorPageResponseInterestDto searchInterests(
//      String keyword,
//      String orderBy,
//      String direction,
//      String cursor,
//      LocalDateTime after,
//      int limit,
//      UUID requestUserId
//  ) {
//    List<Interest> interests = interestRepository.searchInterests(
//        keyword, orderBy, direction, cursor, after, limit
//    );
//
//    List<InterestDto> content = interests.stream()
//        .map(interest -> interestMapper.toDto(interest, interest.get))
//        .toList();
//
//    // 다음 페이지 커서 설정 (마지막 요소 기준)
//    String nextCursor = content.isEmpty() ? null : content.get(content.size() - 1).getId().toString();
//
//    return new CursorPageResponseInterestDto(content, nextCursor);
//    return null;
//  }


  private void validateSimilarNameExists(String name) {
    List<Interest> existingInterests = interestRepository.findAllByDeletedAtFalse();

    for (Interest existingInterest : existingInterests) {
      String existingName = existingInterest.getName();

      // 1단계: 기존 관심사에 등록하려는 이름이 포함되는지 확인
      if (existingName.toLowerCase().contains(name.toLowerCase())) {
        // 2단계: 80% 이상 유사도 확인
        String lowerName = name.toLowerCase().trim();
        String lowerExistingName = existingName.toLowerCase().trim();

        double similarity;
        if (lowerName.equals(lowerExistingName)) {
          similarity = 1.0;
        } else {
          int shorterLength = Math.min(lowerName.length(), lowerExistingName.length());
          int longerLength = Math.max(lowerName.length(), lowerExistingName.length());
          similarity = (double) shorterLength / longerLength;
        }

        if (similarity >= 0.8) {
          throw new InterestException(INTEREST_NAME_DUPLICATION, Map.of("name", name));
        }
      }
    }
  }
}
