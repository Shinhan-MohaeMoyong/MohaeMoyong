package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.domain.Accounts;

@Getter
@Builder
public class SimpleAccountListResponse {
    private String accountNo;
    private Long accountBalance;
    private String accountName;

    public static SimpleAccountListResponse toDto(InquireDemandDepositAccountListResponse.Record record, Accounts ourAccount) {
        // 우리 DB에 저장된 계좌 별칭(accountName)이 있다면 그것을 사용하고,
        // 없다면(null이면) 어댑터에서 받은 계좌명을 예비로 사용
        String finalAccountName = (ourAccount != null) ? ourAccount.getAccountName() : record.getAccountName();

        return SimpleAccountListResponse.builder()
                .accountNo(record.getAccountNo())
                .accountBalance(record.getAccountBalance())
                .accountName(finalAccountName)
                .build();
    }

}
