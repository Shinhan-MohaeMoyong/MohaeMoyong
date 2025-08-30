package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    private String accountName;
    private String accountTypeUniqueNo;
    private Long targetAmount;
    private String accountNo;
}
