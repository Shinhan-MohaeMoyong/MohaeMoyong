package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor
public class AccountNoRequest {
    private String accountNo;
    private String authCode;
}

