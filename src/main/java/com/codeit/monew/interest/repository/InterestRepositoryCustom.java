package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Interest;
import java.time.LocalDateTime;
import java.util.List;

public interface InterestRepositoryCustom {
  List<Interest> findInterestsWithCursor(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      LocalDateTime after,
      int limit
  );
}


