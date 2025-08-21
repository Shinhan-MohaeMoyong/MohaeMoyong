package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.FriendLastSeen;
import shinhan.mohaemoyong.server.domain.PlanOverride;
import shinhan.mohaemoyong.server.domain.PlanSeries;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.FriendPlanDto;
import shinhan.mohaemoyong.server.dto.FriendWeeklyPlanDto;
import shinhan.mohaemoyong.server.dto.OccurrenceDto;
import shinhan.mohaemoyong.server.repository.FriendLastSeenRepository;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;
import shinhan.mohaemoyong.server.repository.PlanOverrideRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendPlanService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final PlanRepository planRepository;
    private final FriendLastSeenRepository lastSeenRepository;
    private final FriendshipRepository friendshipRepository;
    private final PlanOverrideRepository planOverrideRepository;


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

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek   = sunday.atTime(LocalTime.MAX);

        // 1. 친구 일정 (Plan + Series)
        List<Plans> friendPlans = planRepository.findFriendPlansWithinRange(friendId, startOfWeek, endOfWeek);

        // 2. 오버라이드 조회
        List<Long> seriesIds = friendPlans.stream()
                .map(Plans::getPlanSeries)
                .filter(Objects::nonNull)
                .map(PlanSeries::getSeriesId)
                .toList();

        Map<Long, List<PlanOverride>> overridesBySeries = seriesIds.isEmpty()
                ? Collections.emptyMap()
                : planOverrideRepository.findBySeriesIdsInRange(seriesIds, startOfWeek.toLocalDate(), endOfWeek.toLocalDate())
                .stream().collect(Collectors.groupingBy(o -> o.getSeries().getSeriesId()));

        // 3. lastSeen 체크
        FriendLastSeen lastSeen = lastSeenRepository
                .findByUserIdAndFriendId(userId, friendId)
                .orElse(new FriendLastSeen(userId, friendId, LocalDateTime.MIN));

        // 4. occurrence 확장 + override 적용
        List<FriendWeeklyPlanDto> result = friendPlans.stream()
                .flatMap(plan -> {
                    if (plan.getPlanSeries() != null && plan.getPlanSeries().getEnabled()) {
                        List<OccurrenceDto> occs = plan.getPlanSeries().expandOccurrences(startOfWeek, endOfWeek);
                        List<PlanOverride> overrides = overridesBySeries.getOrDefault(plan.getPlanSeries().getSeriesId(), List.of());
                        Map<LocalDate, PlanOverride> byDate = overrides.stream()
                                .collect(Collectors.toMap(PlanOverride::getOccurrenceDate, o -> o));

                        return occs.stream()
                                .filter(occ -> {
                                    PlanOverride ovr = byDate.get(occ.startTime().toLocalDate());
                                    return ovr == null || !ovr.isCancelled();
                                })
                                .map(occ -> {
                                    PlanOverride ovr = byDate.get(occ.startTime().toLocalDate());
                                    LocalDateTime start = ovr != null && ovr.getStartTime() != null ? ovr.getStartTime() : occ.startTime();
                                    LocalDateTime end   = ovr != null && ovr.getEndTime() != null ? ovr.getEndTime() : occ.endTime();
                                    String title       = ovr != null && ovr.getTitle() != null ? ovr.getTitle() : occ.title();
                                    String place       = ovr != null && ovr.getPlace() != null ? ovr.getPlace() : occ.place();

                                    return FriendWeeklyPlanDto.builder()
                                            .planId(plan.getPlanId())
                                            .title(title)
                                            .place(place)
                                            .startTime(start)
                                            .endTime(end)
                                            .isNew(plan.getCreatedAt().isAfter(lastSeen.getLastSeenAt()))
                                            .build();
                                });
                    } else {
                        return Stream.of(FriendWeeklyPlanDto.builder()
                                .planId(plan.getPlanId())
                                .title(plan.getTitle())
                                .place(plan.getPlace())
                                .startTime(plan.getStartTime())
                                .endTime(plan.getEndTime())
                                .isNew(plan.getCreatedAt().isAfter(lastSeen.getLastSeenAt()))
                                .build());
                    }
                })
                .sorted(Comparator.comparing(FriendWeeklyPlanDto::getStartTime))
                .toList();

        // 5. 자동 seen 처리
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
