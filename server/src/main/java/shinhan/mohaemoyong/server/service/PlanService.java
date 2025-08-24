package shinhan.mohaemoyong.server.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.PlanPhotos;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.PlanParticipants;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.*;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public PlanCreateResponse createPlan(Long creatorId, PlanCreateRequest req) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (req.startTime().isAfter(req.endTime())) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        if (Boolean.TRUE.equals(req.hasSavingsGoal()) && req.savingsAmount() == null) {
            throw new IllegalArgumentException("저축 목표가 있으면 금액을 반드시 입력해야 합니다.");
        }
        if (req.type() == PlanType.GROUP && (req.participantIds() == null || req.participantIds().isEmpty())) {
            throw new IllegalArgumentException("단체 일정은 초대할 참여자가 필요합니다.");
        }

        // --- 그룹/친구 상호 검증 ---
        Set<Long> participantIds = new HashSet<>();
        if (req.type() == PlanType.GROUP) {
            participantIds.addAll(req.participantIds());
            participantIds.remove(creatorId); // 중복 방지
            List<Long> notFriends = participantIds.stream()
                    .filter(pid -> !(friendshipRepository.existsEdge(creatorId, pid)
                            && friendshipRepository.existsEdge(pid, creatorId)))
                    .toList();
            if (!notFriends.isEmpty()) {
                throw new IllegalArgumentException("서로 친구가 아닌 사용자가 포함되어 있습니다: " + notFriends);
            }
        }

        // --- 반복 전개 목록 만들기 ---
        List<Occurrence> occs = buildOccurrences(req.startTime(), req.endTime(), req.recurrence());
        final String seriesId = (occs.size() > 1 ? UUID.randomUUID().toString() : null);

        // --- 저장 (각 occurrence마다 개별 plan row 생성) ---
        List<Plans> created = new ArrayList<>(occs.size());
        for (int i = 0; i < occs.size(); i++) {
            Occurrence oc = occs.get(i);

            Plans plan = Plans.builder()
                    .user(creator)
                    .title(req.title())
                    .content(req.content())
                    .imageUrl(req.imageUrl())   // ✅ 대표 이미지
                    .place(req.place())
                    .startTime(oc.start)
                    .endTime(oc.end)
                    .hasSavingsGoal(Boolean.TRUE.equals(req.hasSavingsGoal()))
                    .savingsAmount(req.savingsAmount())
                    .privacyLevel(req.privacyLevel().name())
                    .isCompleted(false)
                    .commentCount(0)
                    .seriesId(seriesId)
                    .occurrenceIndex(seriesId == null ? null : i)
                    .build();

            // ✅ 나머지 사진 저장
            if (req.photos() != null && !req.photos().isEmpty()) {
                int order = 1;
                for (String url : req.photos()) {
                    // imageUrl과 중복되는 건 제외 (대표 이미지는 따로 관리)
                    if (req.imageUrl() != null && req.imageUrl().equals(url)) continue;

                    plan.addPhoto(PlanPhotos.create(plan, url, order++, null, null));
                }
            }


            if (req.type() == PlanType.GROUP) {
                Set<Long> all = new HashSet<>(participantIds);
                all.add(creatorId);
                for (Long uid : all) {
                    User u = (uid.equals(creatorId)) ? creator
                            : userRepository.findById(uid)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: " + uid));
                    PlanParticipants pp = PlanParticipants.builder()
                            .plan(plan)
                            .user(u)
                            .role("member")
                            .build();
                    plan.addParticipant(pp);
                }
            }

            planRepository.save(plan);
            created.add(plan);
        }

        // --- 응답 생성 ---
        List<Long> planIds = created.stream()
                .map(Plans::getPlanId)
                .toList();

        List<CreatedPlanItem> items = created.stream()
                .map(p -> new CreatedPlanItem(
                        p.getPlanId(),
                        p.getOccurrenceIndex(),
                        p.getTitle(),
                        p.getPlace(),
                        p.getStartTime(),
                        p.getEndTime()
                ))
                .toList();

        Plans first = created.get(0);
        return new PlanCreateResponse(
                first.getPlanId(),
                first.getTitle(),
                first.getContent(),
                first.getPlace(),
                first.getStartTime(),
                first.getEndTime(),
                req.privacyLevel(),
                first.isHasSavingsGoal(),
                first.getSavingsAmount(),
                first.getImageUrl(),
                req.participantIds(),
                first.getPhotos().stream()
                        .map(PlanPhotos::getPhotoUrl)
                        .toList(),
                req.recurrence(),
                seriesId,
                created.size(),
                planIds,
                items
        );
    }

    /* --------------------------------- */
    /* 반복 전개 유틸                      */
    /* --------------------------------- */
    private record Occurrence(LocalDateTime start, LocalDateTime end) {}

    private List<Occurrence> buildOccurrences(LocalDateTime start, LocalDateTime end, RecurrenceCreateReq r) {
        List<Occurrence> out = new ArrayList<>();
        if (r == null || !Boolean.TRUE.equals(r.enabled()) || r.count() == null || r.count() <= 1) {
            out.add(new Occurrence(start, end));
            return out;
        }

        final int count = r.count();
        final int interval = (r.interval() == null || r.interval() < 1) ? 1 : r.interval();
        final java.time.Duration dur = java.time.Duration.between(start, end);
        final String freq = (r.freq() == null) ? "DAILY" : r.freq().toUpperCase();

        switch (freq) {
            case "DAILY" -> {
                for (int i = 0; i < count; i++) {
                    LocalDateTime s = start.plusDays((long) i * interval);
                    out.add(new Occurrence(s, s.plus(dur)));
                }
            }
            case "WEEKLY" -> {
                List<java.time.DayOfWeek> order = weeklyOrder(r, start);
                int generated = 0;
                int weekBlock = 0;
                while (generated < count) {
                    LocalDate baseMonday = start.toLocalDate()
                            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            .plusWeeks((long) weekBlock * interval);
                    for (java.time.DayOfWeek dow : order) {
                        LocalDate candidate = baseMonday.with(java.time.temporal.TemporalAdjusters.nextOrSame(dow));
                        if (weekBlock == 0 && candidate.isBefore(start.toLocalDate())) continue;
                        LocalDateTime s = LocalDateTime.of(candidate, start.toLocalTime());
                        out.add(new Occurrence(s, s.plus(dur)));
                        generated++;
                        if (generated >= count) break;
                    }
                    weekBlock++;
                }
            }
            case "MONTHLY" -> {
                for (int i = 0; i < count; i++) {
                    LocalDateTime s = start.plusMonths((long) i * interval);
                    out.add(new Occurrence(s, s.plus(dur)));
                }
            }
            default -> throw new IllegalArgumentException("지원하지 않는 반복 주기: " + freq);
        }
        return out;
    }

    private List<java.time.DayOfWeek> weeklyOrder(RecurrenceCreateReq r, LocalDateTime start) {
        if (r.byDays() == null || r.byDays().isEmpty()) {
            return List.of(start.getDayOfWeek());
        }
        Map<String, java.time.DayOfWeek> map = Map.of(
                "MO", java.time.DayOfWeek.MONDAY,
                "TU", java.time.DayOfWeek.TUESDAY,
                "WE", java.time.DayOfWeek.WEDNESDAY,
                "TH", java.time.DayOfWeek.THURSDAY,
                "FR", java.time.DayOfWeek.FRIDAY,
                "SA", java.time.DayOfWeek.SATURDAY,
                "SU", java.time.DayOfWeek.SUNDAY
        );
        List<java.time.DayOfWeek> ordered = new ArrayList<>();
        List<java.time.DayOfWeek> weekOrder = List.of(
                java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY, java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
        );
        for (java.time.DayOfWeek d : weekOrder) {
            for (String s : r.byDays()) {
                java.time.DayOfWeek m = map.get(s.toUpperCase());
                if (m == d) ordered.add(m);
            }
        }
        if (ordered.isEmpty()) ordered = weekOrder;
        return ordered;
    }

    public List<DetailPlanResponse> selectPlansByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Plans> foundPlans = planRepository.findPlansByDateRangeWithUser(startOfDay, endOfDay);
        return foundPlans.stream()
                .map(this::ToDTODetailPlanResponse)
                .collect(Collectors.toList());
    }

    private DetailPlanResponse ToDTODetailPlanResponse(Plans plan) {
        User author = plan.getUser();
        return new DetailPlanResponse(
                plan.getPlanId(),
                author.getId(),
                author.getName(),
                plan.getTitle(),
                plan.getContent(),
                plan.getImageUrl(),
                plan.getPlace(),
                plan.getStartTime(),
                plan.getEndTime(),
                plan.isCompleted(),
                plan.isHasSavingsGoal(),
                plan.getSavingsAmount(),
                plan.getPrivacyLevel(),
                plan.getCommentCount(),
                List.of()
        );
    }
}
