package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FriendResponse {
    private Long id;
    private String name;
    private String email;
    private String imageUrl;
}
