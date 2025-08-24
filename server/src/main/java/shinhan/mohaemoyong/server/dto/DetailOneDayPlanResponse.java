package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.domain.Plans;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DetailOneDayPlanResponse {
    private Long planId;
    private Long authorId;
    private String title;
    private String place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isCompleted;
    private boolean hasSavingsGoal;
    private Integer savingsAmount;
    private String privacyLevel;
    private String withDrawAccountNo;
    private String depositAccountNo;

    public static DetailOneDayPlanResponse toDto(Plans plans) {
        return DetailOneDayPlanResponse.builder()
                .planId(plans.getPlanId())
                .authorId(plans.getUser().getId())
                .title(plans.getTitle())
                .place(plans.getPlace())
                .startTime(plans.getStartTime())
                .endTime(plans.getEndTime())
                .isCompleted(plans.isCompleted())
                .hasSavingsGoal(plans.isHasSavingsGoal())
                .savingsAmount(plans.getSavingsAmount())
                .privacyLevel(plans.getPrivacyLevel()) // Assuming PrivacyLevel is an enum
                .withDrawAccountNo(plans.getWithdrawAccountNo())
                .depositAccountNo(plans.getDepositAccountNo())
                .build();
    }
}
