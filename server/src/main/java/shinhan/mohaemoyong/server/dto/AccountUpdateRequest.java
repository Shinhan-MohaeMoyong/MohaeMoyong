package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccountUpdateRequest {
    private Long targetAmount;
    private String accountAlias;
}
