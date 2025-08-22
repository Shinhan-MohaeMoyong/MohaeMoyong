package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;

@Getter
@Builder
public class SimpleAccountListResponse {
    private String accountNo;
    private Long accountBalance;
    private String accountName;

    public static SimpleAccountListResponse toDto(InquireDemandDepositAccountListResponse.Record record) {
        return SimpleAccountListResponse.builder()
                .accountNo(record.getAccountNo())
                .accountBalance(record.getAccountBalance())
                .accountName(record.getAccountName())
                .build();


    }

}
