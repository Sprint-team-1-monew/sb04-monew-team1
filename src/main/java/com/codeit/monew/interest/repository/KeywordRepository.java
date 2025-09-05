package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Keyword;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, UUID> {
}