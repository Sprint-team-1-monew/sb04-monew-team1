package com.codeit.monew.interest.repository;

import com.codeit.monew.interest.entity.Interest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom{

  List<Interest> findAllByIsDeletedFalse();

}

