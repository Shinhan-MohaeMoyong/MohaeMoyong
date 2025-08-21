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
        String imageUrl,
        List<Long> participantIds,
        List<String> photos,           // ✅ 여러 장 사진 지원
        RecurrenceCreateReq recurrence // ✅ 반복 정보
) {}
