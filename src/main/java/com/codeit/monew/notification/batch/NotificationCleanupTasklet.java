package com.codeit.monew.notification.batch;

import com.codeit.monew.notification.service.NotificationService;
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

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupTasklet implements Tasklet {

  private final NotificationService notificationService;
  private final MeterRegistry meterRegistry;
  private Counter deletedNotificationCounter;

  // 앱 시작 시 메트릭 초기화
  @PostConstruct
  public void initMetrics() {
    deletedNotificationCounter = Counter.builder("notification.cleanup.deleted.count")
        .description("Deleted notifications count for cleanup batch")
        .register(meterRegistry);
    // 초기값 0
    deletedNotificationCounter.increment(0);
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    int deletedCount = notificationService.deleteOldConfirmNotifications();
    log.info("삭제된 알림 개수: {}", deletedCount);

    // Micrometer Counter 증가
    deletedNotificationCounter.increment(deletedCount);

    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}
