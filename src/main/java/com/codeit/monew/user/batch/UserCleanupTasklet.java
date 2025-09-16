package com.codeit.monew.user.batch;

import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.repository.UserRepository;
import com.codeit.monew.user.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupTasklet implements Tasklet {

  private final UserRepository userRepository;
  private final UserService userService;
  private final MeterRegistry meterRegistry;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    List<User> deletedUsers = userRepository.findUsersForHardDelete(
        LocalDateTime.now().minusMinutes(5));

    int successCount = 0;
    int failureCount = 0;

    for (User user : deletedUsers) {
      try {
        userService.hardDelete(user.getId());
        successCount++;
        log.info("사용자 물리 삭제 성공: {}", user.getId());
      } catch (UserException e) {
        failureCount++;
        String message = String.format("%s 물리 삭제 실패", user.getId().toString());
        log.error("사용자 물리 삭제 실패: {}", user.getId(), e);
        // 개별 실패는 로그만 하고 배치는 계속 진행
      }
    }

    log.info("사용자 정리 배치 완료 - 성공: {}, 실패: {}, 전체: {}",
        successCount, failureCount, deletedUsers.size());

    // 메트릭 기록
    Counter successCounter = meterRegistry.counter("user.cleanup.success.count");
    Counter failureCounter = meterRegistry.counter("user.cleanup.failure.count");
    successCounter.increment(successCount);
    failureCounter.increment(failureCount);

    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}

