package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.domain.User;

@Getter
@Builder
@AllArgsConstructor
public class FriendResponse {
    private Long id;
    private String name;
    private String email;
    private String imageUrl;

    // ✅ User → FriendResponse 변환 메서드
    public static FriendResponse from(User user) {
        return FriendResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())
                .build();
    }
}
