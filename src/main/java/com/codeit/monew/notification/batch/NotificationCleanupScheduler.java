package com.codeit.monew.notification.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

  private final JobLauncher jobLauncher;
  private final Job notificationCleanupJob;

  @Scheduled(cron = "0 0 0 * * ?")
  public void runJob() throws Exception {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();

      JobExecution execution = jobLauncher.run(notificationCleanupJob, jobParameters);

      log.info("알림 정리 배치 완료");
    } catch (Exception e) {
      log.error("알림 정리 배치 실행 실패", e);
    }
  }
}
