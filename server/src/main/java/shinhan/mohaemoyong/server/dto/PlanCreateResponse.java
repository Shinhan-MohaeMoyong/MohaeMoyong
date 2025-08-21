// PlanCreateResponse.java
package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlanCreateResponse(
        Long planId,
        String title,
        String content,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime,
        PrivacyLevel privacyLevel,
        Boolean hasSavingsGoal,
        Integer savingsAmount,
        String imageUrl,               // ✅ 대표 사진(썸네일)
        List<String> photos,           // ✅ 여러 장 URL
        List<Long> participantIds,
        RecurrenceCreateReq recurrence
) {}
