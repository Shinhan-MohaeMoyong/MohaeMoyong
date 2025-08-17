package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.Comments;
import shinhan.mohaemoyong.server.dto.CommentListItemDto;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.CommentPhotoRepository;
import shinhan.mohaemoyong.server.repository.CommentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final CommentPhotoRepository commentPhotoRepository;

    public Page<CommentListItemDto> getComments(Long planId, Pageable pageable, UserPrincipal user) {
        // 1) 댓글 ID만 페이징 (createdAt DESC)
        Page<Long> idPage = commentRepository.findPageIdsByPlanId(planId, pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, idPage.getTotalElements());
        }

        // 2) ID들로 댓글 + 작성자(User) fetch join
        List<Comments> comments = commentRepository.findWithUserByIdIn(ids);

        // 2-1) IN 절은 순서를 보장하지 않으므로, 첫 페이지에서 받은 ids 순서로 정렬 보정
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) order.put(ids.get(i), i);
        comments.sort(Comparator.comparingInt(c -> order.getOrDefault(c.getCommentId(), Integer.MAX_VALUE)));

        // 3) 사진 일괄 조회 후 commentId 별로 그룹핑
        var rows = commentPhotoRepository.findRowsByCommentIds(ids);
        Map<Long, List<CommentListItemDto.PhotoDto>> photoMap = rows.stream()
                .collect(Collectors.groupingBy(
                        CommentPhotoRepository.PhotoRow::getCommentId,
                        Collectors.mapping(r -> new CommentListItemDto.PhotoDto(r.getPhotoUrl(), r.getOrderNo()),
                                Collectors.toList())
                ));

        // 4) DTO 매핑
        List<CommentListItemDto> content = comments.stream().map(c ->
                new CommentListItemDto(
                        c.getCommentId(),
                        c.getUser().getId(),
                        c.getUser().getName(),
                        c.getUser().getImageUrl(),
                        c.getContent(),
                        c.getCreatedAt(),
                        photoMap.getOrDefault(c.getCommentId(), List.of())
                )
        ).toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }
}
