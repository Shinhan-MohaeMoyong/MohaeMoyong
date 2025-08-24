package shinhan.mohaemoyong.server.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record PlanCreateRequest(

        @NotNull PlanType type,                // PERSONAL | GROUP
        @NotNull PrivacyLevel privacyLevel,    // PERSONAL_PUBLIC | GROUP_PUBLIC 등

        @NotBlank String title,
        String content,
        String place,

        String imageUrl,                       // ✅ 대표 이미지 (썸네일)

        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,

        @NotNull Boolean hasSavingsGoal,       // ✅ null 방지
        @Positive Integer savingsAmount,       // hasSavingsGoal=true일 때 필수

        String depositAccountNo,               // 입금 계좌 (hasSavingsGoal=true 시 필수)
        String withdrawalAccountNo,            // 출금 계좌 (hasSavingsGoal=true 시 필수)

        List<Long> participantIds,             // GROUP일 때만 사용

        @Valid RecurrenceCreateReq recurrence, // 반복 정보 (없으면 null)

        List<String> photos                    // ✅ 나머지 사진들 (여러 장)
) {

    // ✅ 조건부 검증
    @AssertTrue(message = "hasSavingsGoal=true일 경우 savingsAmount를 입력해야 합니다.")
    public boolean isSavingsAmountValid() {
        return !Boolean.TRUE.equals(hasSavingsGoal) || savingsAmount != null;
    }

    @AssertTrue(message = "hasSavingsGoal=true일 경우 depositAccountNo를 입력해야 합니다.")
    public boolean isDepositAccountValid() {
        return !Boolean.TRUE.equals(hasSavingsGoal) || (depositAccountNo != null && !depositAccountNo.isBlank());
    }

    @AssertTrue(message = "hasSavingsGoal=true일 경우 withdrawalAccountNo를 입력해야 합니다.")
    public boolean isWithdrawalAccountValid() {
        return !Boolean.TRUE.equals(hasSavingsGoal) || (withdrawalAccountNo != null && !withdrawalAccountNo.isBlank());
    }
}
