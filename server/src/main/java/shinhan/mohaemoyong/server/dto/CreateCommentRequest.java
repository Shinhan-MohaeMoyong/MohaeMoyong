package shinhan.mohaemoyong.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCommentRequest(
        @Size(max = 10_000) String content,
        List<PhotoItem> photos
) {
    public record PhotoItem(
            @NotBlank String url,
            Integer orderNo
    ) {}
}