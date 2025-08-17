package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.FriendLastSeen;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.FriendWeeklyPlanDto;
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

    /**
     * 이번주에 친구가 새 일정 등록했는지 확인 (빨간테두리 생성 여부)
     */
    public boolean hasNewPlanThisWeek(Long userId, Long friendId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek   = sunday.atTime(LocalTime.MAX);

        List<Plans> recentPlans = planRepository.findRecentPublicPlansThisWeek(friendId, startOfWeek, endOfWeek);
        if (recentPlans.isEmpty()) return false;

        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.MIN));

        return recentPlans.stream()
                .anyMatch(p -> p.getCreatedAt().isAfter(lastSeen.getLastSeenAt()));
    }

    /**
     * 친구 일정 확인 처리 → 빨간테두리 제거
     */
    public void markPlansAsSeen(Long userId, Long friendId) {
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.now()));

        lastSeen.updateLastSeen(LocalDateTime.now());
        lastSeenRepository.save(lastSeen);
    }

    /**
     * 친구의 이번주 일정 조회 (일정별로 '새로운 일정 여부' 플래그 포함)
     */
    public List<FriendWeeklyPlanDto> getFriendWeeklyPlansWithNewFlag(Long userId, Long friendId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek   = sunday.atTime(LocalTime.MAX);

        // 이번주 PUBLIC 일정
        List<Plans> plans = planRepository.findRecentPublicPlansThisWeek(friendId, startOfWeek, endOfWeek);

        // 내가 마지막으로 본 시각
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.MIN));

        return plans.stream()
                .map(p -> FriendWeeklyPlanDto.builder()
                        .planId(p.getPlanId())
                        .title(p.getTitle())
                        .place(p.getPlace())
                        .startTime(p.getStartTime())
                        .endTime(p.getEndTime())
                        .isNew(p.getCreatedAt().isAfter(lastSeen.getLastSeenAt())) // 새 일정 여부
                        .build()
                )
                .toList();
    }
}
