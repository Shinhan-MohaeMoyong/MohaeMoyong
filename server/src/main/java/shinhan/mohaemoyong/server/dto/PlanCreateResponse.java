package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlanCreateResponse(
        // ── 기존 단일용 필드 (그대로 유지) ─────────────────────────────
        Long planId,
        String title,
        String content,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime,
        PrivacyLevel privacyLevel,
        Boolean hasSavingsGoal,
        Integer savingsAmount,
        String depositAccountNo,        // ✅ 추가
        String imageUrl,
        List<Long> participantIds,
        List<String> photos,
        RecurrenceCreateReq recurrence,

        String seriesId,                 // 단일 생성이면 null
        Integer createdCount,            // 생성된 일정 개수
        List<Long> planIds,              // 생성된 모든 planId
        List<CreatedPlanItem> items      // 생성된 일정 요약
) {}
