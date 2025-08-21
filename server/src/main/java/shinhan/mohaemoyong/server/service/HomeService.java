package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.PlanOverride;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;
import shinhan.mohaemoyong.server.repository.PlanOverrideRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;
import shinhan.mohaemoyong.server.dto.OccurrenceDto;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final PlanRepository planRepository;
    private final PlanOverrideRepository planOverrideRepository;

    // 이번주 일정 조회
    public List<HomeWeekResponse> getThisWeekPlans(Long userId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek = sunday.atTime(LocalTime.MAX);

        // 1. 기본 단일 일정 조회
        List<Plans> plans = planRepository.findPlansWithinRange(userId, startOfWeek, endOfWeek)
                .stream()
                .filter(p -> p.getDeletedAt() == null) // ✅ 삭제된 일정 제거
                .toList();

        // 2. occurrence 전개
        return plans.stream()
                .flatMap(plan -> {
                    var series = plan.getPlanSeries();
                    if (series != null && Boolean.TRUE.equals(series.getEnabled())) {
                        return series.expandOccurrences(startOfWeek, endOfWeek).stream()
                                .map(HomeWeekResponse::fromOccurrence);
                    } else {
                        return java.util.stream.Stream.of(
                                new HomeWeekResponse(
                                        plan.getPlanId(),
                                        plan.getTitle(),
                                        plan.getPlace(),
                                        plan.getStartTime(),
                                        plan.getEndTime()
                                )
                        );
                    }
                })
                .sorted(java.util.Comparator.comparing(HomeWeekResponse::getStartTime))
                .toList();
    }



        // 전체 일정 조회 (기본: 오늘-6개월 ~ 오늘+6개월)
        public List<HomeWeekResponse> getAllPlans(Long userId) {
            ZoneId zone = ZoneId.of("Asia/Seoul");
            LocalDate today = LocalDate.now(zone);

            LocalDateTime from = today.minusMonths(6).atStartOfDay();
            LocalDateTime to   = today.plusMonths(6).atTime(LocalTime.MAX);

            return getAllPlans(userId, from, to);
        }

    // 전체 일정 조회 (범위 지정 버전)
    public List<HomeWeekResponse> getAllPlans(Long userId,
                                              LocalDateTime from,
                                              LocalDateTime to) {
        // 1) 원본 일정들 로드 (삭제 제외)
        List<Plans> plans = planRepository.findAllPlansEntityByUserId(userId);

        // 2) 단일 + 반복 전개 + override 반영
        return plans.stream()
                .flatMap(plan -> {
                    var series = plan.getPlanSeries();

                    // 반복 없음 ⇒ 단일 일정이 범위에 걸리면 그대로 반환
                    if (series == null || !Boolean.TRUE.equals(series.getEnabled())) {
                        if (plan.getEndTime().isBefore(from) || plan.getStartTime().isAfter(to)) {
                            return java.util.stream.Stream.<HomeWeekResponse>empty();
                        }
                        return java.util.stream.Stream.of(new HomeWeekResponse(
                                plan.getPlanId(),
                                plan.getTitle(),
                                plan.getPlace(),
                                plan.getStartTime(),
                                plan.getEndTime()
                        ));
                    }

                    // 반복 있음 ⇒ occurrence 전개
                    // 반복 있음 ⇒ occurrence 전개
                    var occs = series.expandOccurrences(from, to);

                // 해당 범위의 override 가져와서 map으로
                    var overrides = planOverrideRepository.findBySeries_SeriesIdAndOccurrenceDateBetween(
                            series.getSeriesId(), from.toLocalDate(), to.toLocalDate());

                    var ovByDate = overrides.stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    PlanOverride::getOccurrenceDate, o -> o, (a, b) -> a));

                    return occs.stream()
                            // 취소된 회차 제외
                                    .filter(occ -> {
                                        var ov = ovByDate.get(occ.startTime().toLocalDate());
                                        return ov == null || !ov.isCancelled();
                                    })

                                    // override 값 있으면 덮어쓰기
                            .map(occ -> {
                                var ov = ovByDate.get(occ.startTime().toLocalDate());
                                if (ov != null && !ov.isCancelled()) {
                                    return new HomeWeekResponse(
                                            occ.planId(),
                                            ov.getTitle() != null ? ov.getTitle() : occ.title(),
                                            ov.getPlace() != null ? ov.getPlace() : occ.place(),
                                            ov.getStartTime() != null ? ov.getStartTime() : occ.startTime(),
                                            ov.getEndTime() != null ? ov.getEndTime() : occ.endTime()
                                    );
                                }
                                return HomeWeekResponse.fromOccurrence(occ);
                            });

                })
                .sorted(java.util.Comparator.comparing(HomeWeekResponse::getStartTime))
                .toList();
    }
}
