package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.DetailPlanResponse;
import shinhan.mohaemoyong.server.dto.DetailPlanUpdateRequest;
import shinhan.mohaemoyong.server.exception.ResourceNotFoundException;
import shinhan.mohaemoyong.server.repository.PlanRepository;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DetailPlanService {

    private final PlanRepository planRepository;
    private static final Set<String> PRIVACY_ALLOWED = Set.of("PUBLIC", "PRIVATE");

    @Transactional(readOnly = true)
    public DetailPlanResponse getDetail(Long userId, Long planId) {
        // DetailPlanService.java
        Plans p = planRepository.findDetailByOwner(userId, planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + userId)
                );

        return toResponse(p);
    }

    @Transactional
    public DetailPlanResponse update(Long loginUserId, Long planId, DetailPlanUpdateRequest req) {
        Plans p = planRepository.findDetailByOwner(loginUserId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + loginUserId));

        if (p.isDeleted()) throw new ResourceNotFoundException("Plans", "planId", planId);
        if (req.startTime() != null && req.endTime() != null && req.startTime().isAfter(req.endTime()))
            throw new IllegalArgumentException("startTime must be before endTime");

        if (req.privacyLevel() != null) {
            String normalized = req.privacyLevel().trim().toUpperCase();
            if (!PRIVACY_ALLOWED.contains(normalized)) {
                throw new IllegalArgumentException("privacyLevel must be one of " + PRIVACY_ALLOWED);
            }

            req = new DetailPlanUpdateRequest(
                    req.title(), req.content(), req.imageUrl(), req.place(),
                    req.startTime(), req.endTime(), req.isCompleted(),
                    req.hasSavingsGoal(), req.savingsAmount(), normalized
            );
        }

        p.applyUpdate(req, LocalDateTime.now());
        return toResponse(p);
    }

    @Transactional
    public void delete(Long loginUserId, Long planId) {
        Plans p = planRepository.findDetailByOwner(loginUserId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + loginUserId));
        p.softDelete(LocalDateTime.now());
    }

    private DetailPlanResponse toResponse(Plans p) {
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
                p.getPrivacyLevel(), // "PUBLIC" / "PRIVATE"
                p.getCommentCount() == null ? 0 : p.getCommentCount()
        );
    }

}
