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

    // 공통 친구 검증
    private void ensureFriendship(Long viewerId, Long friendId) {
        boolean isFriend =
                friendshipRepository.existsByUserIdAndFriendId(viewerId, friendId) ||
                        friendshipRepository.existsByUserIdAndFriendId(friendId, viewerId);
        if (!isFriend) throw new AccessDeniedException("친구 관계가 아닙니다.");
    }

    // 이번주 새로운 일정 여부 확인
    @Transactional(readOnly = true)
    public boolean hasNewPlanThisWeek(Long userId, Long friendId) {
        ensureFriendship(userId, friendId);

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        // 🔥 오늘 ~ +7일 범위
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = today.plusDays(7).atTime(LocalTime.MAX);

        List<Plans> recentPlans =
                planRepository.findRecentPublicPlansWithinRange(friendId, start, end);
        if (recentPlans.isEmpty()) return false;

        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.MIN));

        return recentPlans.stream()
                .anyMatch(p -> p.getCreatedAt().isAfter(lastSeen.getLastSeenAt()));
    }


    // 친구 일정 확인 → lastSeen 업데이트
    @Transactional
    public void markPlansAsSeen(Long userId, Long friendId) {
        ensureFriendship(userId, friendId);

        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.now()));

        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);
    }

    // 이번주 친구 일정 + isNew 플래그
    @Transactional
    public List<FriendWeeklyPlanDto> getFriendWeeklyPlansWithNewFlag(Long userId, Long friendId) {
        ensureFriendship(userId, friendId);

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        // 🔥 오늘 ~ +7일 범위
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = today.plusDays(7).atTime(LocalTime.MAX);

        List<Plans> plans = planRepository.findRecentPublicPlansWithinRange(friendId, start, end);

        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.MIN));

        List<FriendWeeklyPlanDto> result = plans.stream()
                .map(p -> FriendWeeklyPlanDto.builder()
                        .planId(p.getPlanId())
                        .title(p.getTitle())
                        .place(p.getPlace())
                        .startTime(p.getStartTime())
                        .endTime(p.getEndTime())
                        .isNew(p.getCreatedAt().isAfter(lastSeen.getLastSeenAt()))
                        .build())
                .toList();

        // 🔥 조회 후 자동 seen 처리
        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);

        return result;
    }


    // 전체 공개 일정 조회
    @Transactional
    public List<FriendPlanDto> getFriendAllPublicPlans(Long viewerId, Long friendId) {
        ensureFriendship(viewerId, friendId);

        List<FriendPlanDto> plans = planRepository.findAllPublicPlansOfUser(friendId);

        // 🔥 조회 후 자동 seen 처리
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(viewerId, friendId)
                .orElse(new FriendLastSeen(viewerId, friendId, LocalDateTime.now()));
        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);

        return plans;
    }

}
