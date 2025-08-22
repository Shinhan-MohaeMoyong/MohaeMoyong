package shinhan.mohaemoyong.server.adapter.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMemberResponse {
    private String userId;
    private String userName;    // 사용자 이메일
    private String institutionCode;
    private String userKey;
    private String created;
    private String modified;


}