package com.codeit.monew.subscriptions.repository;

import com.codeit.monew.interest.entity.Interest;
import com.codeit.monew.subscriptions.entity.Subscription;
import com.codeit.monew.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  boolean existsByUserAndInterest(User user, Interest interest);

  Optional<Subscription> findByUserAndInterest(User user, Interest interest);

  List<Subscription> findAllByInterest(Interest interest);

  List<Subscription> findAllByInterestId(UUID interestId);
}
