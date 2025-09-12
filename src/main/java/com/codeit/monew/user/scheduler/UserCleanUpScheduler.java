package com.codeit.monew.user.scheduler;

import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserErrorCode;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.service.UserService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCleanUpScheduler {

  private final UserRepository userRepository;

  private final UserService userService;

  //환경 변수로 변경 예정
  @Scheduled(cron = "0 * * * * *")
  public void cleanUpSoftDeletedUser() {
    List<User> deletedUsers = userRepository.findUsersForHardDelete(LocalDateTime.now().minusMinutes(5));

    for(User user : deletedUsers) {
      try {
        userService.hardDelete(user.getId());
      } catch(UserException e) {
        String message = String.format("%s 물리 삭제 실패",user.getId().toString());
        throw new UserException(UserErrorCode.USER_HARD_DELETE_FAILED, Map.of("user", message));
      }
    }

  }

}
