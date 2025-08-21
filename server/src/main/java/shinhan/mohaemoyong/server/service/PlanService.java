package shinhan.mohaemoyong.server.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.*;
import shinhan.mohaemoyong.server.dto.*;
import shinhan.mohaemoyong.server.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PlanSeriesRepository planSeriesRepository;
    private final PlanOverrideRepository planOverrideRepository;

    /**
     * 일정 생성
     */
    @Transactional
    public PlanCreateResponse createPlan(Long creatorId, PlanCreateRequest req) {
        // 1) 생성자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2) 기본 검증
        if (req.startTime().isAfter(req.endTime())) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        if (Boolean.TRUE.equals(req.hasSavingsGoal()) && req.savingsAmount() == null) {
            throw new IllegalArgumentException("저축 목표가 있으면 금액을 반드시 입력해야 합니다.");
        }
        if (req.type() == PlanType.GROUP && (req.participantIds() == null || req.participantIds().isEmpty())) {
            throw new IllegalArgumentException("단체 일정은 초대할 참여자가 필요합니다.");
        }

        // 3) 일정 엔티티 생성
        Plans plan = Plans.builder()
                .user(creator)
                .title(req.title())
                .content(req.content())
                .place(req.place())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .hasSavingsGoal(Boolean.TRUE.equals(req.hasSavingsGoal()))
                .savingsAmount(req.savingsAmount())
                .privacyLevel(req.privacyLevel().name())
                .isCompleted(false)
                .commentCount(0)
                .build();

        // 4) 사진 여러 장 처리 (+ 대표 이미지)
        if (req.photoUrls() != null && !req.photoUrls().isEmpty()) {
            // 대표 사진(썸네일)
            plan.changeThumbnailTo(req.photoUrls().get(0));

            // 나머지 사진 PlanPhotos 저장
            int order = 1;
            for (int i = 1; i < req.photoUrls().size(); i++) {
                plan.addPhoto(req.photoUrls().get(i), order++, null, null);
            }
        }

        // 먼저 plan 저장 (photos는 cascade로 함께 저장)
        planRepository.save(plan);

        // 5) 단체 일정 처리
        if (req.type() == PlanType.GROUP) {
            Set<Long> ids = new HashSet<>(req.participantIds());
            ids.add(creatorId); // 생성자 자동 포함

            for (Long pid : ids) {
                if (pid.equals(creatorId)) continue;

                User participant = userRepository.findById(pid)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: " + pid));

                // 친구 여부 검증
                boolean isFriend =
                        friendshipRepository.existsByUserAndFriend(creator, participant)
                                && friendshipRepository.existsByUserAndFriend(participant, creator);



                PlanParticipants pp = PlanParticipants.builder()
                        .plan(plan)
                        .user(participant)
                        .role("member")
                        .build();

                plan.addParticipant(pp);
            }
        }

        // 6) 반복 시리즈 저장
        if (req.recurrence() != null && Boolean.TRUE.equals(req.recurrence().enabled())) {
            PlanSeries series = PlanSeries.builder()
                    .plan(plan)
                    .enabled(true)
                    .freq(req.recurrence().freq())
                    .intervalN(req.recurrence().interval() == null ? 1 : req.recurrence().interval())
                    .byDays(req.recurrence().byDays() == null ? null : String.join(",", req.recurrence().byDays()))
                    .untilDate(req.recurrence().until())
                    .countN(req.recurrence().count())
                    .tz("Asia/Seoul")
                    .build();
            planSeriesRepository.save(series);

            if (req.recurrence().exceptions() != null && !req.recurrence().exceptions().isEmpty()) {
                for (LocalDate ex : req.recurrence().exceptions()) {
                    planOverrideRepository.save(
                            PlanOverride.builder()
                                    .series(series)
                                    .occurrenceDate(ex)
                                    .cancelled(true)
                                    .build()
                    );
                }
            }
        }

        // 7) 응답 반환
        return new PlanCreateResponse(
                plan.getPlanId(),
                plan.getTitle(),
                plan.getContent(),
                plan.getPlace(),
                plan.getStartTime(),
                plan.getEndTime(),
                req.privacyLevel(),
                plan.isHasSavingsGoal(),
                plan.getSavingsAmount(),
                plan.getImageUrl(),
                plan.getPhotos().stream()
                        .map(PlanPhotos::getPhotoUrl)
                        .collect(Collectors.toList()),
                req.participantIds() != null ? req.participantIds() : List.of(),
                req.recurrence()
        );
    }

    /**
     * 반복 일정 수정
     */
    @Transactional
    public void patchOccurrence(Long userId, Long planId, OccurrencePatchRequest req) {
        PlanSeries series = planSeriesRepository.findByPlan_PlanId(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정에 반복 시리즈가 없습니다."));

        switch (req.scope()) {
            case THIS_ONLY -> {
                PlanOverride override = planOverrideRepository
                        .findBySeries_SeriesIdAndOccurrenceDate(series.getSeriesId(), req.occurrenceDate())
                        .orElse(PlanOverride.builder()
                                .series(series)
                                .occurrenceDate(req.occurrenceDate())
                                .cancelled(false)
                                .build());

                if (req.title() != null) override = override.toBuilder().title(req.title()).build();
                if (req.content() != null) override = override.toBuilder().content(req.content()).build();
                if (req.place() != null) override = override.toBuilder().place(req.place()).build();
                if (req.startTime() != null) override = override.toBuilder().startTime(req.startTime()).build();
                if (req.endTime() != null) override = override.toBuilder().endTime(req.endTime()).build();
                if (req.privacyLevel() != null) override = override.toBuilder().privacyLevel(req.privacyLevel().name()).build();
                if (req.imageUrl() != null) override = override.toBuilder().imageUrl(req.imageUrl()).build();

                planOverrideRepository.save(override);
            }
            case THIS_AND_FUTURE -> {
                // 🔹 기존 시리즈 잘라내기
                LocalDate newUntil = req.occurrenceDate().minusDays(1);
                series.updateUntil(newUntil);
                planSeriesRepository.save(series);

                // 🔹 Plan 복제 (새 Plan 생성)
                Plans original = series.getPlan();
                Plans newPlan = Plans.builder()
                        .user(original.getUser())
                        .title(req.title() != null ? req.title() : original.getTitle())
                        .content(req.content() != null ? req.content() : original.getContent())
                        .place(req.place() != null ? req.place() : original.getPlace())
                        .startTime(req.startTime() != null ? req.startTime() : original.getStartTime())
                        .endTime(req.endTime() != null ? req.endTime() : original.getEndTime())
                        .hasSavingsGoal(original.isHasSavingsGoal())
                        .savingsAmount(original.getSavingsAmount())
                        .privacyLevel(req.privacyLevel() != null ? req.privacyLevel().name() : original.getPrivacyLevel())
                        .isCompleted(false)
                        .commentCount(0)
                        .imageUrl(req.imageUrl() != null ? req.imageUrl() : original.getImageUrl())
                        .build();

                planRepository.save(newPlan);

                // 🔹 새 PlanSeries 생성
                PlanSeries newSeries = PlanSeries.builder()
                        .plan(newPlan)   // ❗ 새 Plan에 연결
                        .enabled(true)
                        .freq(series.getFreq())
                        .intervalN(series.getIntervalN())
                        .byDays(series.getByDays())
                        .untilDate(series.getUntilDate())
                        .countN(series.getCountN())
                        .tz(series.getTz())
                        .build();

                planSeriesRepository.save(newSeries);

                // 🔹 override 적용 (분할 시점에 바로 patch 반영)
                PlanOverride override = PlanOverride.builder()
                        .series(newSeries)
                        .occurrenceDate(req.occurrenceDate())
                        .cancelled(false)
                        .title(req.title())
                        .content(req.content())
                        .place(req.place())
                        .startTime(req.startTime())
                        .endTime(req.endTime())
                        .privacyLevel(req.privacyLevel() != null ? req.privacyLevel().name() : null)
                        .imageUrl(req.imageUrl())
                        .build();
                planOverrideRepository.save(override);
            }
            case ALL -> {
                Plans plan = series.getPlan();
                plan.applyUpdate(
                        new DetailPlanUpdateRequest(
                                req.title(),
                                req.content(),
                                req.imageUrl(),
                                req.place(),
                                req.startTime(),
                                req.endTime(),
                                null,   // isCompleted
                                null,   // hasSavingsGoal
                                null,   // savingsAmount
                                req.privacyLevel() != null ? req.privacyLevel().name() : null
                        ),
                        LocalDateTime.now()
                );

                planRepository.save(plan);
            }
        }
    }

    /**
     * 반복 일정 삭제
     */
    @Transactional
    public void deleteOccurrence(Long userId, Long planId, OccurrencePatchRequest req) {
        PlanSeries series = planSeriesRepository.findByPlan_PlanId(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정에 반복 시리즈가 없습니다."));

        switch (req.scope()) {
            case THIS_ONLY -> {
                PlanOverride override = planOverrideRepository
                        .findBySeries_SeriesIdAndOccurrenceDate(series.getSeriesId(), req.occurrenceDate())
                        .orElse(PlanOverride.builder()
                                .series(series)
                                .occurrenceDate(req.occurrenceDate())
                                .build());
                override = override.toBuilder().cancelled(true).build();
                planOverrideRepository.save(override);
            }
            case THIS_AND_FUTURE -> {
                LocalDate newUntil = req.occurrenceDate().minusDays(1);
                series.updateUntil(newUntil);
                planSeriesRepository.save(series);
            }
            case ALL -> {
                series.disable();
                planSeriesRepository.save(series);

                Plans plan = series.getPlan();
                plan.softDelete(LocalDateTime.now());
                planRepository.save(plan);
            }
        }
    }
}
