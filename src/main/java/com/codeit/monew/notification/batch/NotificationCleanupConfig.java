package com.codeit.monew.notification.batch;

import com.codeit.monew.notification.service.NotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NotificationCleanupConfig extends DefaultBatchConfiguration {

  @Bean
  public Job notificationCleanupJob(JobRepository jobRepository, Step cleanupStep) {
    return new JobBuilder("notificationCleanupJob", jobRepository)
        .start(cleanupStep)
        .build();
  }

  @Bean
  public Step cleanupStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      NotificationService notificationService) {

    return new StepBuilder("cleanupStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          notificationService.deleteOldConfirmNotifications();
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }
}
