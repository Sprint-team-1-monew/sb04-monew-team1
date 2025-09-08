package com.codeit.monew.interest.repository;

import com.codeit.monew.article.entity.Article;
import com.codeit.monew.interest.entity.Interest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

}
