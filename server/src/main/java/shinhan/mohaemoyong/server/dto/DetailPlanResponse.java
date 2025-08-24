package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        Integer commentCount,
        List<PhotoDto> photos
) {
    public record PhotoDto(
            Long photoId,
            String url,
            Integer orderNo,
            Integer width,
            Integer height
    ) {}
}