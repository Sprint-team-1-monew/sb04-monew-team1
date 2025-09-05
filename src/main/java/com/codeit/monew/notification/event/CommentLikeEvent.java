package com.codeit.monew.notification.event;

import java.util.UUID;

public record CommentLikeEvent(
    UUID userId,
    UUID commentId,
    UUID likeByUserId
) {}
