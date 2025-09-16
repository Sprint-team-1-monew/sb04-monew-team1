package com.codeit.monew.article.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class ArticleCleanupConfig extends DefaultBatchConfiguration {

  @Bean
  public Job articleCleanUpJob(JobRepository jobRepository, Step articleCleanUpStep) {
    log.info("ArticleCleanUpJob");
    return new JobBuilder("articleCleanUpJob", jobRepository)
        .start(articleCleanUpStep)
        .build();
  }

  @Bean
  public Step articleCleanUpStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ArticleCleanUpTasklet articleCleanupTasklet) {
    log.info("ArticleCleanUpStep");
    return new StepBuilder("articleCleanUpStep", jobRepository)
        .tasklet(articleCleanupTasklet, transactionManager)
        .build();
  }
}
