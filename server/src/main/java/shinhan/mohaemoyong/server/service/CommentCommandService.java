package shinhan.mohaemoyong.server.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import shinhan.mohaemoyong.server.domain.Comments;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.CreateCommentRequest;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.CommentRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long planId, UserPrincipal me, CreateCommentRequest req) {
        User user = userRepository.findById(me.getId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Plans plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 일정입니다."));

        Comments comment = Comments.create(plan, user, req.content())
                .addPhotos(req.photos());

        commentRepository.save(comment);
        planRepository.incrementCommentCount(planId);
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
