package shinhan.mohaemoyong.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetailPlanUpdateRequest(
        String title,
        String content,
        String imageUrl,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Boolean isCompleted,
        Boolean hasSavingsGoal,
        Integer savingsAmount,
        String privacyLevel,   // "PRIVATE" | "PUBLIC"
        List<UpdatePhotoItem> photos
) {
    public record UpdatePhotoItem(
        Long photoId,
        String url,
        Integer orderNo,
        Integer width,
        Integer height
    ) {}
}
