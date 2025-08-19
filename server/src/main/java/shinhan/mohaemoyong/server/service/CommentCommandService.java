package shinhan.mohaemoyong.server.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import shinhan.mohaemoyong.server.domain.Comments;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.CommentRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final PlanRepository planRepository;

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
