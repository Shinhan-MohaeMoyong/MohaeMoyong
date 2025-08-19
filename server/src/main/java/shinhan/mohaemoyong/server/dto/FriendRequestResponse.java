package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.domain.FriendRequest;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class FriendRequestResponse {
    private Long requestId;
    private Long requesterId;
    private String requesterName;
    private String status;
    private String message;
    private Instant createdAt;

    public static FriendRequestResponse from(FriendRequest request) {
        return FriendRequestResponse.builder()
                .requestId(request.getRequestId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getName())
                .status(request.getStatus().name())
                .message(request.getMessage())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
