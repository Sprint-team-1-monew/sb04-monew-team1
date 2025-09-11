package com.codeit.monew.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.monew.notification.service.NotificationService;
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
  private MeterRegistry meterRegistry;

  @InjectMocks
  private NotificationCleanupTasklet tasklet;

  @Test
  @DisplayName("Tasklet 실행 시 NotificationService 호출되고 ExitStatus=COMPLETED 이어야 한다")
  void execute_shouldCallNotificationServiceAndReturnCompleted() throws Exception {
    // given
    StepContribution contribution = mock(StepContribution.class);
    ChunkContext chunkContext = mock(ChunkContext.class);

    // meterRegistry.counter()가 호출되면 NPE 방지
    io.micrometer.core.instrument.Counter counterMock = mock(io.micrometer.core.instrument.Counter.class);
    when(meterRegistry.counter("notification.cleanup.deleted.count")).thenReturn(counterMock);

    // when
    RepeatStatus status = tasklet.execute(contribution, chunkContext);

    // then
    verify(notificationService, times(1)).deleteOldConfirmNotifications();
    verify(counterMock, times(1)).increment(anyDouble());
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);

    // ExitStatus=COMPLETED 명시적 세팅 확인
    verify(contribution).setExitStatus(ExitStatus.COMPLETED);
  }
}
