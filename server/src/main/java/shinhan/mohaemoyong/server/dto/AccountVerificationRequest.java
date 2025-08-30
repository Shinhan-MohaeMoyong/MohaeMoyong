package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerificationRequest {
    private String accountNo;
    private String authCode;
}