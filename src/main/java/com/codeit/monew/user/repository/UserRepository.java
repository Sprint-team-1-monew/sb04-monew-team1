package com.codeit.monew.user.repository;

import com.codeit.monew.user.entity.User;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.userStatus = 'DELETED' AND u.deletedAt <= :now")
  List<User> findUsersForHardDelete(LocalDateTime now);
}
