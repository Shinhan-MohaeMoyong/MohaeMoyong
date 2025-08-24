package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.PrivacyLevel;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;
import shinhan.mohaemoyong.server.repository.PlanParticipantsRepository;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final FriendshipRepository friendshipRepository;
    private final PlanParticipantsRepository planParticipantsRepository;

    public boolean canViewPlan(Plans plan, UserPrincipal userPrincipal) {
        Long viewerId = userPrincipal.getId();
        Long ownerId  = plan.getUser().getId();

        if (ownerId.equals(viewerId)) return true;

        final PrivacyLevel level = parseLevel(plan.getPrivacyLevel());

        return switch (level) {
            case PERSONAL_PUBLIC  -> isFriend(viewerId, ownerId);
            case PERSONAL_PRIVATE -> false;

            case GROUP_PUBLIC     ->
                // ✅ 참여자이거나, 소유자의 친구이거나, 참여자 중 누군가의 친구
                    isParticipant(plan.getPlanId(), viewerId)
                            || isFriend(viewerId, ownerId)
                            || isFriendWithAnyParticipant(plan.getPlanId(), viewerId);

            case GROUP_PRIVATE    ->
                // ✅ 참여자만
                    isParticipant(plan.getPlanId(), viewerId);
        };
    }

    private PrivacyLevel parseLevel(String level) {
        try {
            return PrivacyLevel.valueOf(level);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected privacyLevel: " + level);
        }
    }

    private boolean isFriend(Long a, Long b) {
        return friendshipRepository.countActiveBetween(a, b) > 0;
    }

    private boolean isParticipant(Long planId, Long userId) {
        return planParticipantsRepository.existsByPlan_PlanIdAndUser_Id(planId, userId);
    }

    private boolean isFriendWithAnyParticipant(Long planId, Long viewerId) {
        return planParticipantsRepository.existsFriendWithAnyParticipant(planId, viewerId);
    }
}