package shinhan.mohaemoyong.server.adapter.user.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IssuedApiKeyResponse {
    private String managerId;       // 관리자 ID
    private String apiKey;          // 발급된 api 키
    private String creationDate;    // 생성일
    private String expirationDate;  // 만료일
}
