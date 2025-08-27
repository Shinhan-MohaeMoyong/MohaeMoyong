package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DepositRequest {
    private String withdrawAccountNo;
}


