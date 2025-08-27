package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.domain.FriendRequest;

import java.time.Instant;

import static org.springframework.web.servlet.function.ServerResponse.status;

@Getter
@Builder
@AllArgsConstructor
public class FriendRequestResponse {
    private Long requestId;
    private Long requesterId;
    private String requesterImgUrl;
    private String requesterName;
    private Long receiverId;
    private String receiverName;
    private String receiverImgUrl;
    private String receiverEmail;
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

    public static FriendRequestResponse fromRequest(FriendRequest r) {
        return FriendRequestResponse.builder()
                .requestId(r.getRequestId())
                .requesterId(r.getRequester().getId())
                .requesterName(r.getRequester().getName())
                .requesterImgUrl(r.getRequester().getImageUrl())
                .receiverId(r.getReceiver().getId())
                .receiverName(r.getReceiver().getName())
                .receiverImgUrl(r.getReceiver().getImageUrl())
                .receiverEmail(r.getReceiver().getEmail())
                .status(r.getStatus().name())
                .message(r.getMessage())
                .createdAt(r.getCreatedAt())
                .build();
    }

    public static FriendRequestResponse fromEntity(FriendRequest r) {
        return FriendRequestResponse.builder()
                .requestId(r.getRequestId())
                .requesterId(r.getRequester().getId())
                .requesterName(r.getRequester().getName())
                .receiverId(r.getReceiver().getId())
                .receiverName(r.getReceiver().getName())
                .receiverImgUrl(r.getReceiver().getImageUrl())
                .receiverEmail(r.getReceiver().getEmail())
                .status(r.getStatus().name())
                .message(r.getMessage())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

//@Getter
//@Builder
//@AllArgsConstructor
//public class FriendRequestResponse {
//    private Long requestId;
//    private Long requesterId;
//    private String requesterImgUrl;
//    private String requesterName;
//    private Long receiverId;
//    private String receiverName;
//    private String receiverImgUrl;
//    private String receiverEmail;
//    private String status;
//    private String message;
//    private Instant createdAt;
//
//    public static FriendRequestResponse of(FriendRequest r) {
//        return FriendRequestResponse.builder()
//                .requestId(r.getRequestId())
//
//                .requesterId(r.getRequester().getId())
//                .requesterName(r.getRequester().getName())
//                .requesterImgUrl(r.getRequester().getImageUrl())
//
//                .receiverId(r.getReceiver().getId())
//                .receiverName(r.getReceiver().getName())
//                .receiverImgUrl(r.getReceiver().getImageUrl())
//                .receiverEmail(r.getReceiver().getEmail())
//
//                .status(r.getStatus().name())
//                .message(r.getMessage())
//                .createdAt(r.getCreatedAt())
//                .build();
//    }
//}
