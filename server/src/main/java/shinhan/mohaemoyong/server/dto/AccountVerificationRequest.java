package shinhan.mohaemoyong.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountVerificationRequest {
    private String accountNo;
    private String authCode;
}