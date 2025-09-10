package com.codeit.monew.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.monew.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "spring.task.scheduling.enabled=false",
    "spring.quartz.job-store-type=memory",
    "spring.quartz.auto-startup=false"
})
@SpringBatchTest
class NotificationCleanupJobTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @MockitoBean
  private NotificationService notificationService; // Spring 컨텍스트에 Mock 등록

  @Test
  @DisplayName("NotificationCleanupJob 실행 시 NotificationService 호출 검증")
  void notificationCleanupJob_ServiceCalled() throws Exception {
    // when
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParameters());

    // then
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

    // NotificationService.deleteOldConfirmNotifications()가 호출됐는지 확인
    verify(notificationService, times(1)).deleteOldConfirmNotifications();
  }
}
