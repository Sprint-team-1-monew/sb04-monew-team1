package com.codeit.monew.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.notification.service.NotificationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupTaskletTest {

  @Mock
  private NotificationService notificationService;

  @Mock
  private io.micrometer.core.instrument.Counter deletedNotificationCounter;

  @Mock
  private MeterRegistry meterRegistry;

  @InjectMocks
  private NotificationCleanupTasklet tasklet;

  @Test
  @DisplayName("Tasklet 실행 시 NotificationService 호출되고 Counter increment, ExitStatus=COMPLETED 확인")
  void execute_shouldCallNotificationServiceAndIncrementCounter() throws Exception {
    // given
    StepContribution contribution = mock(StepContribution.class);
    ChunkContext chunkContext = mock(ChunkContext.class);

    // notificationService.deleteOldConfirmNotifications()가 5 반환
    when(notificationService.deleteOldConfirmNotifications()).thenReturn(5);

    // tasklet 내부 counter를 직접 주입
    Counter counterMock = mock(Counter.class);
    when(meterRegistry.counter("notification.cleanup.deleted.count")).thenReturn(counterMock);

    // when
    RepeatStatus status = tasklet.execute(contribution, chunkContext);

    // then
    verify(notificationService, times(1)).deleteOldConfirmNotifications();
    verify(counterMock, times(1)).increment(anyDouble()); // 삭제 개수 증가 호출 확인
    verify(contribution).setExitStatus(ExitStatus.COMPLETED);
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
  }
}
