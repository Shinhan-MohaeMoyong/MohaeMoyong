package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;

@Service
@RequiredArgsConstructor
public class AccessControlService {
    private final FriendshipRepository friendshipRepository;

    public boolean canViewPlan(Plans plan, UserPrincipal userPrincipal) {
        if (plan.getUser().getId().equals(userPrincipal.getId())) return true;

        return switch (plan.getPrivacyLevel()) {
            case "PUBLIC" -> friendshipRepository.existsFriendEdge(userPrincipal.getId(), plan.getUser().getId());
            case "PRIVATE" -> false;

            default -> throw new IllegalStateException("Unexpected value: " + plan.getPrivacyLevel());
        };
    }
}