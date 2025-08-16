package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.DetailPlanResponse;
import shinhan.mohaemoyong.server.exception.ResourceNotFoundException;
import shinhan.mohaemoyong.server.repository.PlanRepository;

@Service
@RequiredArgsConstructor
public class DetailPlanService {

    private final PlanRepository planRepository;

    @Transactional(readOnly = true)
    public DetailPlanResponse getDetail(Long userId, Long planId) {
        // DetailPlanService.java
        Plans p = planRepository.findDetailByOwner(userId, planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + userId)
                );


        return new DetailPlanResponse(
                p.getPlanId(),
                p.getUser().getId(),
                p.getUser().getName(),
                p.getTitle(),
                p.getContent(),
                p.getImageUrl(),
                p.getPlace(),
                p.getStartTime(),
                p.getEndTime(),
                p.isCompleted(),
                p.isHasSavingsGoal(),
                p.getSavingsAmount(),
                p.getPrivacyLevel(),
                p.getCommentCount() == null ? 0 : p.getCommentCount()
        );
    }
}
