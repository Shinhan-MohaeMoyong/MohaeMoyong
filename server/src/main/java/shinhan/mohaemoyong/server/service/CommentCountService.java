package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.dto.CommentCountResponse;
import shinhan.mohaemoyong.server.exception.ResourceNotFoundException;
import shinhan.mohaemoyong.server.repository.CommentRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentCountService {
    private final CommentRepository commentRepository;
    public CommentCountResponse getCommentCount(Long planId) {
        long count = commentRepository.findCommentCountByPlanId(planId)
                .map(Integer::longValue)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "planId", planId));

        return new CommentCountResponse(planId, count);
    }
}