package com.codeit.monew.user.batch;

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
public class UserCleanupScheduler {

  private final JobLauncher jobLauncher;
  private final Job userCleanupJob;

  @Scheduled(cron = "0 * * * * *") // 매분 실행 (환경 변수로 변경 예정)
  public void runJob() throws Exception {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();

      JobExecution execution = jobLauncher.run(userCleanupJob, jobParameters);

      log.info("사용자 정리 배치 완료 - Status: {}, ExitCode: {}",
          execution.getStatus(), execution.getExitStatus().getExitCode());
    } catch (Exception e) {
      log.error("사용자 정리 배치 실행 실패", e);
      throw e; // 스케줄러에서 예외를 다시 던져 모니터링 도구에서 감지 가능
    }
  }
}
