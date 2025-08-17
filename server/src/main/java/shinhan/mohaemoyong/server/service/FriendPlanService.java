package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.FriendLastSeen;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.repository.FriendLastSeenRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendPlanService {

    private final PlanRepository planRepository;
    private final FriendLastSeenRepository lastSeenRepository;

    /** 이번주 새 일정이 있는지 확인 (빨간테두리용) */
    public boolean hasNewPlanThisWeek(Long userId, Long friendId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek   = sunday.atTime(LocalTime.MAX);

        // 이번주 일정 중 가장 최신 생성된 일정들
        List<Plans> recentPlans = planRepository.findRecentPublicPlansThisWeek(friendId, startOfWeek, endOfWeek);

        if (recentPlans.isEmpty()) return false;

        // 마지막 본 시간
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.MIN));

        // 하나라도 lastSeen 이후에 생성됐다면 → 새로운 일정 있음
        return recentPlans.stream()
                .anyMatch(p -> p.getCreatedAt().isAfter(lastSeen.getLastSeenAt()));
    }

    /** 친구 일정 확인 처리 → 빨간테두리 제거 */
    public void markPlansAsSeen(Long userId, Long friendId) {
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.now()));

        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);
    }
}
