package shinhan.mohaemoyong.server.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import shinhan.mohaemoyong.server.domain.CommentPhotos;
import shinhan.mohaemoyong.server.domain.Comments;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.CreateCommentRequest;
import shinhan.mohaemoyong.server.dto.UpdateCommentRequest;
import shinhan.mohaemoyong.server.exception.BadRequestException;
import shinhan.mohaemoyong.server.exception.ResourceNotFoundException;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.CommentPhotoRepository;
import shinhan.mohaemoyong.server.repository.CommentRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;
    private final CommentPhotoRepository commentPhotoRepository;

    @Transactional
    public void createComment(Long planId, UserPrincipal userPrincipal, @Valid CreateCommentRequest req) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Plans plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 일정입니다."));

        Comments comment = Comments.create(plan, user, req.content())
                .addPhotos(req.photos());

        // plan 접근 권한 설정
        if (!accessControlService.canViewPlan(plan, userPrincipal)) {
            throw new ResourceNotFoundException("Plans", "planId", planId);
        }

        commentRepository.save(comment);
        planRepository.incrementCommentCount(planId);
    }

    @Transactional
    public void updateComment(Long planId, Long commentId, UserPrincipal me, UpdateCommentRequest req) {
        // 1) 댓글 로드 (+소프트삭제 제외)
        Comments comment = commentRepository
                .findByIdAndPlan_PlanIdAndDeletedAtIsNull(commentId, planId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));

        // 2) 권한 체크: 작성자 본인만 수정 (필요 시 관리자/플랜소유자 허용 로직 추가)
        if (!comment.getUser().getId().equals(me.getId())) {
            throw new ForbiddenException("댓글 수정 권한이 없습니다.");
        }

        // 3) 내용 수정 (null이면 무시)
        if (req.getContent() != null) {
            String newContent = req.getContent().trim();
            if (newContent.isEmpty()) {
                throw new BadRequestException("내용은 비어 있을 수 없습니다.");
            }
            comment.updateContent(newContent);
        }

        // 4) 이미지 추가
        if (req.getAddImageUrls() != null && !req.getAddImageUrls().isEmpty()) {
            int nextOrder = commentPhotoRepository.findMaxOrderNoByCommentId(commentId) + 1;

            for (String url : req.getAddImageUrls()) {
                CommentPhotos photo = CommentPhotos.create(url, nextOrder++);
                photo.setCommentInternal(comment);             // 연관관계 주인 세팅
                commentPhotosRepository.save(photo);           // 명시 저장
            }
        }

        // 5) 이미지 삭제
        if (req.getRemoveImageIds() != null && !req.getRemoveImageIds().isEmpty()) {
            List<CommentImage> toDelete = commentImageRepository
                    .findAllByIdInAndComment_CommentId(req.getRemoveImageIds(), comment.getCommentId());
            if (toDelete.size() != req.getRemoveImageIds().size()) {
                throw new BadRequestException("삭제 대상 이미지 중 존재하지 않는 항목이 있습니다.");
            }
            commentImageRepository.deleteAllInBatch(toDelete);
        }
    }

    @Transactional
    public void deleteComment(Long planId, Long commentId, UserPrincipal me) {
        Comments comment = commentRepository.findActiveByIdAndPlanId(commentId, planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없거나 이미 삭제되었습니다."));

        Long ownerId = comment.getUser().getId();
        boolean isOwner = ownerId.equals(me.getId());
        boolean isAdmin = me.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        // 소프트 삭제
        comment.softDelete();

        // 댓글 수 캐시 감소
        planRepository.decrementCommentCountSafely(comment.getPlan().getPlanId());
    }
}
