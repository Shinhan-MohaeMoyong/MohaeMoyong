package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.PlanPhotos;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.DetailPlanResponse;
import shinhan.mohaemoyong.server.dto.DetailPlanUpdateRequest;
import shinhan.mohaemoyong.server.exception.ResourceNotFoundException;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.PlanPhotoRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetailPlanService {

    private final PlanRepository planRepository;
    private final AccessControlService accessControlService;
    private final PlanPhotoRepository planPhotoRepository;
    private static final Set<String> PRIVACY_ALLOWED = Set.of("PUBLIC", "PRIVATE");

    @Transactional(readOnly = true)
    public DetailPlanResponse getDetail(UserPrincipal userPrincipal, Long planId) {
        // DetailPlanService.java
        Plans p = planRepository.findDetailByOwner(userPrincipal.getId(), planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + userPrincipal.getId())
                );

        // plan 접근 권한 설정
        if (!accessControlService.canViewPlan(p, userPrincipal)) {
            throw new ResourceNotFoundException("Plans", "planId", planId);
        }

        // 사진들 조회 (정렬 보장)
        List<PlanPhotos> photos = planPhotoRepository
                .findByPlan_PlanIdOrderByOrderNoAscPlanPhotoIdAsc(planId);

        return toResponse(p, photos);
    }

    @Transactional
    public DetailPlanResponse update(UserPrincipal userPrincipal, Long planId, DetailPlanUpdateRequest req) {
        Plans p = planRepository.findDetailByOwner(userPrincipal.getId(), planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + userPrincipal.getId()));

        if (p.isDeleted()) throw new ResourceNotFoundException("Plans", "planId", planId);
        if (req.startTime() != null && req.endTime() != null && req.startTime().isAfter(req.endTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        // privacyLevel 정규화
        if (req.privacyLevel() != null) {
            String normalized = req.privacyLevel().trim().toUpperCase();
            if (!PRIVACY_ALLOWED.contains(normalized)) {
                throw new IllegalArgumentException("privacyLevel must be one of " + PRIVACY_ALLOWED);
            }
            req = new DetailPlanUpdateRequest(
                    req.title(), req.content(), req.imageUrl(), req.place(),
                    req.startTime(), req.endTime(), req.isCompleted(),
                    req.hasSavingsGoal(), req.savingsAmount(), normalized,
                    req.photos()
            );
        }

        // 1) 스칼라 필드 갱신 (엔티티 도메인 메서드 사용, 세터 금지)
        p.applyUpdate(req, LocalDateTime.now());

        // 2) 사진 치환: 요청에 photos가 들어온 경우에만 수행
        if (req.photos() != null) {
            // 현재 사진 조회 및 맵 구성
            List<PlanPhotos> current =
                    planPhotoRepository.findByPlan_PlanIdOrderByOrderNoAscPlanPhotoIdAsc(planId);

            Map<Long, PlanPhotos> currentById = current.stream()
                    .filter(ph -> ph.getPlanPhotoId() != null)
                    .collect(Collectors.toMap(PlanPhotos::getPlanPhotoId, ph -> ph));

            // 중복 photoId 방지
            var seen = new HashSet<Long>();
            for (var it : req.photos()) {
                if (it.photoId() != null && !seen.add(it.photoId())) {
                    throw new IllegalArgumentException("Duplicated photoId in request: " + it.photoId());
                }
            }

            var next = new ArrayList<PlanPhotos>();
            var incomingIds = new HashSet<Long>();

            for (int i = 0; i < req.photos().size(); i++) {
                var it = req.photos().get(i);
                Integer orderNo = (it.orderNo() != null) ? it.orderNo() : i;

                if (it.photoId() != null) {
                    // 기존 엔티티 수정: 도메인 메서드 사용 (세터 금지)
                    PlanPhotos existing = currentById.get(it.photoId());
                    if (existing == null) {
                        throw new IllegalArgumentException("photoId not found for this plan: " + it.photoId());
                    }
                    existing.updateAttributes(it.url(), orderNo, it.width(), it.height());
                    next.add(existing);
                    incomingIds.add(existing.getPlanPhotoId());
                } else {
                    // 신규 추가: 정적 팩토리 사용 (세터/빌더 체인 금지)
                    if (it.url() == null || it.url().isBlank()) {
                        throw new IllegalArgumentException("New photo requires non-empty url");
                    }
                    PlanPhotos created = PlanPhotos.create(p, it.url(), orderNo, it.width(), it.height());
                    next.add(created);
                }
            }

            // 삭제 대상 산출: 기존 - (요청에 포함된 photoId)
            List<PlanPhotos> toDelete = current.stream()
                    .filter(ph -> ph.getPlanPhotoId() != null && !incomingIds.contains(ph.getPlanPhotoId()))
                    .toList();
            if (!toDelete.isEmpty()) {
                planPhotoRepository.deleteAll(toDelete);
            }

            // 신규만 선별
            List<PlanPhotos> toInsert = next.stream()
                    .filter(ph -> ph.getPlanPhotoId() == null)
                    .toList();

            // ✅ 방어: plan 누락 시 즉시 실패 (DB까지 안 가게)
            toInsert.forEach(ph -> {
                if (ph.getPlan() == null) {
                    throw new IllegalStateException("PlanPhoto.plan is null (check PlanPhotos.create)");
                }
            });

            if (!toInsert.isEmpty()) {
                planPhotoRepository.saveAll(toInsert);
            }
            // 기존 수정분은 JPA dirty checking으로 처리됨
        }

        // 최신 사진 포함 응답
        List<PlanPhotos> photos =
                planPhotoRepository.findByPlan_PlanIdOrderByOrderNoAscPlanPhotoIdAsc(planId);

        return toResponse(p, photos);
    }

//    @Transactional
//    public DetailPlanResponse update(UserPrincipal userPrincipal, Long planId, DetailPlanUpdateRequest req) {
//        Plans p = planRepository.findDetailByOwner(userPrincipal.getId(), planId)
//                .orElseThrow(() -> new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + userPrincipal.getId()));
//
//        if (p.isDeleted()) throw new ResourceNotFoundException("Plans", "planId", planId);
//        if (req.startTime() != null && req.endTime() != null && req.startTime().isAfter(req.endTime()))
//            throw new IllegalArgumentException("startTime must be before endTime");
//
//        if (req.privacyLevel() != null) {
//            String normalized = req.privacyLevel().trim().toUpperCase();
//            if (!PRIVACY_ALLOWED.contains(normalized)) {
//                throw new IllegalArgumentException("privacyLevel must be one of " + PRIVACY_ALLOWED);
//            }
//
//            req = new DetailPlanUpdateRequest(
//                    req.title(), req.content(), req.imageUrl(), req.place(),
//                    req.startTime(), req.endTime(), req.isCompleted(),
//                    req.hasSavingsGoal(), req.savingsAmount(), normalized
//            );
//        }
//
//        p.applyUpdate(req, LocalDateTime.now());
//
//        // 업데이트 후에도 사진 포함해 응답
//        List<PlanPhotos> photos = planPhotoRepository
//                .findByPlan_PlanIdOrderByOrderNoAscPlanPhotoIdAsc(planId);
//
//        return toResponse(p, photos);
//    }

    @Transactional
    public void delete(UserPrincipal userPrincipal, Long planId) {
        Plans p = planRepository.findDetailByOwner(userPrincipal.getId(), planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plans", "planId/userId", planId + "/" + userPrincipal.getId()));

        p.softDelete(LocalDateTime.now());
    }

    private DetailPlanResponse toResponse(Plans p, List<PlanPhotos> photos) {
        // 대표 이미지가 비어있으면 첫 번째 사진으로 대체(선택)
        String imageUrl = p.getImageUrl();
        if (imageUrl == null && photos != null && !photos.isEmpty()) {
            imageUrl = photos.get(0).getPhotoUrl();
        }

        List<DetailPlanResponse.PhotoDto> photoDtos = photos == null ? List.of() :
                photos.stream()
                        .map(ph -> new DetailPlanResponse.PhotoDto(
                                ph.getPlanPhotoId(),
                                ph.getPhotoUrl(),
                                ph.getOrderNo(),
                                ph.getWidth(),
                                ph.getHeight()
                        ))
                        .toList();

        return new DetailPlanResponse(
                p.getPlanId(),
                p.getUser().getId(),
                p.getUser().getName(),
                p.getTitle(),
                p.getContent(),
                p.getImageUrl(),
                p.getPlace(),
                p.getStartTime(),
                p.getEndTime(),
                p.isCompleted(),
                p.isHasSavingsGoal(),
                p.getSavingsAmount(),
                p.getPrivacyLevel(), // "PUBLIC" / "PRIVATE"
                p.getCommentCount() == null ? 0 : p.getCommentCount(),
                photoDtos
        );
    }

}
