package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;

public record DetailPlanResponse(
        Long planId,
        Long authorId,
        String authorName,
        String title,
        String content,
        String imageUrl,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isCompleted,
        boolean hasSavingsGoal,
        Integer savingsAmount,
        String privacyLevel,
        Integer commentCount
) {}