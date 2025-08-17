package shinhan.mohaemoyong.server.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public record CommentListItemDto(
        Long commentId,
        Long userId,
        String userName,
        String userImageUrl,
        String content,
        LocalDateTime createdAt,
        List<PhotoDto> photos
) {
    public record PhotoDto(String photoUrl, Integer orderNo) {}
}