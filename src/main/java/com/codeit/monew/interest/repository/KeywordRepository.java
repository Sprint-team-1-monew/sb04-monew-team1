package com.codeit.monew.interest.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.interest.entity.Keyword;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, UUID> {
  
  List<Keyword> findByInterest(Interest interest); // 기사 쪽에서 쓰임
  
}

