package com.codeit.monew.notification.event;

import com.codeit.monew.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @EventListener
  public void handleArticleCreatedEvent(ArticleCreatedEvent event){
    log.info("Received ArticleCreatedEvent: {}", event);
    notificationService.createInterestArticleNotification(
        event.userId(),
        event.interestId(),
        event.articleCount()
    );
  }

  @EventListener
  public void handleCommentLikeEvent(CommentLikeEvent event){
    log.info("Received CommentLikeEvent: {}", event);
    notificationService.createCommentLikeNotification(
        event.userId(),
        event.commentId(),
        event.likeByUserId()
    );
  }
}
