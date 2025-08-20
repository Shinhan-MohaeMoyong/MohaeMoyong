package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long commentId,
        Long planId,
        UserDto user,
        String content,
        LocalDateTime createdAt,
        List<PhotoDto> photos
) {
    public record UserDto(Long userId, String name, String imageUrl) {}
    public record PhotoDto(Long photoId, String url, Integer orderNo) {}
}