package com.codeit.monew.notification.batch;

import com.codeit.monew.notification.service.NotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
public class NotificationCleanupTasklet implements Tasklet {

  private final NotificationService notificationService;
  private final MeterRegistry meterRegistry;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    // 삭제된 알림 수 조회
    int deletedCount = notificationService.deleteOldConfirmNotifications();
    log.info("삭제된 알림 개수: {}", deletedCount);

    // Tasklet 안에서 Counter 생성 후 increment
    Counter counter = meterRegistry.counter("notification.cleanup.deleted.count");
    counter.increment(deletedCount);

    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}
