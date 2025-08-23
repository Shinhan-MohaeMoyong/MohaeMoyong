package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountCreateRequest {
    private String accountName;
}
