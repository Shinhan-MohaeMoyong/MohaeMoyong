package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shinhan.mohaemoyong.server.dto.CommentCountResponse;
import shinhan.mohaemoyong.server.dto.CommentListItemDto;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.CommentCountService;
import shinhan.mohaemoyong.server.service.CommentQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plans/{planId}/comments")
public class CommentController {

    private final CommentCountService commentCountService;
    private final CommentQueryService commentQueryService;

    // 댓글 수 조회
    @GetMapping("/count")
    public CommentCountResponse getCommentCount(@PathVariable Long planId,
                                                @CurrentUser UserPrincipal user) {
        return commentCountService.getCommentCount(planId);
    }

    // 댓글 목록 조회
    @GetMapping
    public Page<CommentListItemDto> getComments(
            @PathVariable Long planId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @CurrentUser UserPrincipal user
    ) {
        return commentQueryService.getComments(planId, pageable);

    }
}