package shinhan.mohaemoyong.server.dto.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String userkey;
    private String email;
    private String imageUrl;

    public static UserResponse toDto(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getName())
                .userkey(user.getUserkey())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())

                .build();
    }

}