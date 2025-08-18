package shinhan.mohaemoyong.server.adapter.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IssuedApiKeyRequest{
    private String managerId;   // 관리자 ID(이메일 형식)
}