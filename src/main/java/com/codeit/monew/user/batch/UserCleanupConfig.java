package com.codeit.monew.user.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UserCleanupConfig extends DefaultBatchConfiguration {
  @Bean
  public Step userCleanupStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      UserCleanupTasklet userCleanupTasklet) {

    return new StepBuilder("userCleanupStep", jobRepository)
        .tasklet(userCleanupTasklet, transactionManager)
        .build();
  }

  @Bean
  public Job userCleanupJob(JobRepository jobRepository, Step userCleanupStep) {
    //cleanupStep을 파라미터 명으로 사용하면 notification의 cleanupStep을 주입받음
    return new JobBuilder("userCleanupJob", jobRepository)
        .start(userCleanupStep)
        .build();
  }
}
