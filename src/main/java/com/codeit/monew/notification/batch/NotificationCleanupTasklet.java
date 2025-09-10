package com.codeit.monew.notification.batch;

import com.codeit.monew.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationCleanupTasklet implements Tasklet {

  private final NotificationService notificationService;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    notificationService.deleteOldConfirmNotifications();
    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}
