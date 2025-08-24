package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.PlanPhotos;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;
import shinhan.mohaemoyong.server.repository.PlanRepository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final PlanRepository planRepository;

    // 이번주 일정 조회
    public List<HomeWeekResponse> getThisWeekPlans(Long userId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime endOfWeek   = sunday.atTime(LocalTime.MAX);

        List<Plans> plans = planRepository.findPlansByUserAndDateRange(userId, startOfWeek, endOfWeek);

        return plans.stream()
                .map(p -> new HomeWeekResponse(
                        p.getPlanId(),
                        p.getSeriesId(),
                        p.getOccurrenceIndex(),
                        p.getTitle(),
                        p.getContent(),
                        p.getPlace(),
                        p.getStartTime(),
                        p.getEndTime(),
                        p.getImageUrl(),
                        p.getPhotos().stream()
                                .map(PlanPhotos::getPhotoUrl)
                                .toList()
                ))
                .collect(Collectors.toList());
    }

    // 전체 일정 조회
    public List<HomeWeekResponse> getAllPlans(Long userId) {
        List<Plans> plans = planRepository.findPlansByUserIdWithPhotos(userId);

        return plans.stream()
                .map(p -> new HomeWeekResponse(
                        p.getPlanId(),
                        p.getSeriesId(),
                        p.getOccurrenceIndex(),
                        p.getTitle(),
                        p.getContent(),
                        p.getPlace(),
                        p.getStartTime(),
                        p.getEndTime(),
                        p.getImageUrl(),
                        p.getPhotos().stream()
                                .map(PlanPhotos::getPhotoUrl)
                                .toList()
                ))
                .collect(Collectors.toList());
    }
}
