package shinhan.mohaemoyong.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FriendRequestDto {
    private Long receiverId;
    private String message;
}
