package shinhan.mohaemoyong.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record PlanCreateRequest(

        @NotNull PlanType type,                // PERSONAL | GROUP
        @NotNull PrivacyLevel privacyLevel,    // PERSONAL_PUBLIC | ...

        @NotBlank String title,
        String content,
        String place,

        String imageUrl,                       // ✅ 대표 이미지 (썸네일)

        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,

        Boolean hasSavingsGoal,
        @Positive Integer savingsAmount,       // hasSavingsGoal=true일 때 필수

        List<Long> participantIds,             // GROUP일 때만 사용

        @Valid RecurrenceCreateReq recurrence, // 반복 정보 (없으면 null)

        List<String> photos                    // ✅ 나머지 사진들 (여러 장)
) {}
