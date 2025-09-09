package com.codeit.monew.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codeit.monew.notification.entity.Notification;
import com.codeit.monew.notification.entity.ResourceType;
import com.codeit.monew.notification.repository.NotificationRepository;
import com.codeit.monew.user.entity.User;
import com.codeit.monew.user.entity.UserStatus;
import com.codeit.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "spring.task.scheduling.enabled=false",
    "spring.quartz.job-store-type=memory",
    "spring.quartz.auto-startup=false"
})
@SpringBatchTest
@ActiveProfiles("test")
class NotificationCleanupJobTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  @MockitoBean
  private NotificationCleanupScheduler notificationCleanupScheduler;

  @AfterEach
  void tearDown() {
    notificationRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("NotificationCleanupJob 실행 시 확인된지 7일 지난 알림만 정확히 삭제된다")
  void notificationCleanupJob_Success() throws Exception {
    // given
    User user = User.builder()
        .email("test@example.com")
        .nickname("테스터")
        .password("password")
        .userStatus(UserStatus.ACTIVE)
        .build();
    userRepository.save(user);

    // 1. 삭제 대상: 확인했고, 8일 전에 업데이트됨
    Notification oldConfirmed = Notification.builder()
        .user(user)
        .confirmed(true)
        .content("삭제될 알림")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();
    Notification toDelete = notificationRepository.save(oldConfirmed);
    notificationRepository.updateUpdatedAt(toDelete.getId(), LocalDateTime.now().minusDays(8));

    // 2. 삭제 대상 아님: 확인했지만 1일 전에 업데이트됨
    Notification recentConfirmed = Notification.builder()
        .user(user)
        .confirmed(true)
        .content("유지될 확인 알림")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();
    Notification savedRecent = notificationRepository.save(recentConfirmed);
    notificationRepository.updateUpdatedAt(savedRecent.getId(), LocalDateTime.now().minusDays(1));

    // 3. 삭제 대상 아님: 8일 전 알림이지만 확인 안 됨
    Notification notConfirmed = Notification.builder()
        .user(user)
        .confirmed(false)
        .content("유지될 미확인 알림")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();
    Notification savedNotConfirmed = notificationRepository.save(notConfirmed);
    notificationRepository.updateUpdatedAt(savedNotConfirmed.getId(), LocalDateTime.now().minusDays(8));

    // when
    JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParameters();
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // then
    List<Notification> allNotifications = notificationRepository.findAll();

    assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    assertThat(allNotifications).hasSize(2);
    assertThat(allNotifications)
        .extracting("id")
        .doesNotContain(toDelete.getId());
  }
}
