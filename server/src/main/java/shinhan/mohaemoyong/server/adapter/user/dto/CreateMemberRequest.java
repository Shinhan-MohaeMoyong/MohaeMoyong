package shinhan.mohaemoyong.server.adapter.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class CreateMemberRequest {
    // 요청 메시지 명세
    private String apiKey;
    private String userId;
}