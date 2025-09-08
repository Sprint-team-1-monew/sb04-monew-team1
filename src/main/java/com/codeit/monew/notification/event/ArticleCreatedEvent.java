package com.codeit.monew.notification.event;

import java.util.UUID;

public record ArticleCreatedEvent(
    UUID userId,        // 알림 받을 사용자
    UUID interestId,    // 관련 관심사
    String interestName,
    int articleCount
) {}
