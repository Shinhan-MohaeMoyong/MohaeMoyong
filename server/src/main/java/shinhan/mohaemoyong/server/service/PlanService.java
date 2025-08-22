package shinhan.mohaemoyong.server.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.PlanParticipants;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.*;
import shinhan.mohaemoyong.server.repository.PlanRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlanCreateResponse createPlan(Long creatorId, PlanCreateRequest req) {
        // 1. 생성자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 기본 검증
        if (req.startTime().isAfter(req.endTime())) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        if (Boolean.TRUE.equals(req.hasSavingsGoal()) && req.savingsAmount() == null) {
            throw new IllegalArgumentException("저축 목표가 있으면 금액을 반드시 입력해야 합니다.");
        }
        if (req.type() == PlanType.GROUP && (req.participantIds() == null || req.participantIds().isEmpty())) {
            throw new IllegalArgumentException("단체 일정은 초대할 참여자가 필요합니다.");
        }

        // 3. 일정 엔티티 생성
        Plans plan = Plans.builder()
                .user(creator)   // 생성자(작성자)
                .title(req.title())
                .content(req.content())
                .imageUrl(req.imageUrl())
                .place(req.place())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .hasSavingsGoal(Boolean.TRUE.equals(req.hasSavingsGoal()))
                .savingsAmount(req.savingsAmount())
                .privacyLevel(req.privacyLevel().name())
                .isCompleted(false)   // 기본값
                .commentCount(0)      // 기본값
                .build();


        // 4. 저장
        planRepository.save(plan);

        // 5. 단체 일정 처리
        if (req.type() == PlanType.GROUP) {
            // 중복 방지를 위해 Set 사용
            Set<Long> ids = new HashSet<>(req.participantIds());
            ids.add(creatorId); // 생성자 자동 포함
            for (Long pid : ids) {
                User participant = userRepository.findById(pid)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: " + pid));
                PlanParticipants pp = PlanParticipants.builder()
                        .plan(plan)
                        .user(participant)
                        .role("member")   // 안 넣으면 기본값 그대로
                        .build();

                plan.addParticipant(pp); // Plans 엔티티에서 participants 리스트 관리

            }
        }

        // (TODO: RecurrenceCreateReq 반복 일정 처리 추가)

        // 6. 응답 생성
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
                plan.getImageUrl(),         // ✅ imageUrl 추가
                req.participantIds(),       // ✅ 그룹일 경우만 값 존재
                List.of(),                  // ✅ photos (추후 확장: plan.getPhotos() 매핑)
                req.recurrence()            // ✅ 반복 정보 그대로 echo-back
        );

    }
}
