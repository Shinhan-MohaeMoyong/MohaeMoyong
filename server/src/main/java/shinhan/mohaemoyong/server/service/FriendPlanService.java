package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.FriendLastSeen;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.FriendPlanDto;
import shinhan.mohaemoyong.server.dto.FriendWeeklyPlanDto;
import shinhan.mohaemoyong.server.repository.FriendLastSeenRepository;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendPlanService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final PlanRepository planRepository;
    private final FriendLastSeenRepository lastSeenRepository;
    private final FriendshipRepository friendshipRepository;

    /** 👥 친구 관계 검증 */
    private void ensureFriendship(Long viewerId, Long friendId) {
        boolean isFriend =
                friendshipRepository.existsByUserIdAndFriendId(viewerId, friendId) ||
                        friendshipRepository.existsByUserIdAndFriendId(friendId, viewerId);
        if (!isFriend) {
            throw new AccessDeniedException("친구 관계가 아닙니다.");
        }
    }

    /** 📌 이번주 새로운 일정 여부 확인 (개인 + 그룹 공개 포함) */
    @Transactional(readOnly = true)
    public boolean hasNewPlanThisWeek(Long viewerId, Long friendId) {
        ensureFriendship(viewerId, friendId);

        LocalDateTime now   = LocalDateTime.now(ZONE);
        LocalDateTime until = now.plusDays(7);

        LocalDateTime lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(viewerId, friendId)
                .map(FriendLastSeen::getLastSeenAt)
                .orElse(LocalDateTime.of(1970,1,1,0,0));

        // ✅ 존재 여부만 확인 (빠름)
        return planRepository.existsNewPublicPlansForFriendWithin7Days(friendId, now, until, lastSeen);
    }


    /** 📌 친구 일정 확인 → lastSeen 업데이트 */
    @Transactional
    public void markPlansAsSeen(Long userId, Long friendId) {
        ensureFriendship(userId, friendId);

        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.now()));

        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);
    }

    /** 📌 이번주 친구 일정 + isNew 플래그 (개인 + 그룹 공개 포함) */
    @Transactional
    public List<FriendWeeklyPlanDto> getFriendWeeklyPlansWithNewFlag(Long viewerId, Long friendId) {
        ensureFriendship(viewerId, friendId);

        LocalDateTime now   = LocalDateTime.now(ZONE);
        LocalDateTime until = now.plusDays(7);

        List<Plans> plans = planRepository.findFriendPublicPlansWithin7Days(friendId, now, until);

        LocalDateTime lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(viewerId, friendId)
                .map(FriendLastSeen::getLastSeenAt)
                .orElse(LocalDateTime.of(1970,1,1,0,0));

        List<FriendWeeklyPlanDto> result = plans.stream()
                .map(p -> FriendWeeklyPlanDto.builder()
                        .planId(p.getPlanId())
                        .title(p.getTitle())
                        .place(p.getPlace())
                        .startTime(p.getStartTime())
                        .endTime(p.getEndTime())
                        .isNew(p.getCreatedAt() != null && p.getCreatedAt().isAfter(lastSeen))
                        .build())
                .toList();

        // ✅ 조회 후 자동 seen 처리
        FriendLastSeen entity = lastSeenRepository
                .findByUserIdAndFriendId(viewerId, friendId)
                .orElse(new FriendLastSeen(viewerId, friendId, LocalDateTime.of(1970,1,1,0,0)));
        entity.updateLastSeen(LocalDateTime.now(ZONE));
        lastSeenRepository.save(entity);

        return result;
    }


    /** 📌 친구 전체 공개 일정 조회 (개인 + 그룹) */
    @Transactional
    public List<FriendPlanDto> getFriendAllPublicPlans(Long viewerId, Long friendId) {
        ensureFriendship(viewerId, friendId);

        List<Plans> plans = planRepository.findFriendAllPublicPlans(friendId);

        List<FriendPlanDto> result = plans.stream()
                .map(p -> new FriendPlanDto(
                        p.getPlanId(),
                        p.getTitle(),
                        p.getPlace(),
                        p.getStartTime(),
                        p.getEndTime()
                ))
                .toList();

        // ✅ 조회 후 자동 seen 처리
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(viewerId, friendId)
                .orElse(new FriendLastSeen(viewerId, friendId, LocalDateTime.now()));
        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);

        return result;
    }
}
