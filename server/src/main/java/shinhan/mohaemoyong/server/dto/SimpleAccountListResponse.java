package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.domain.Accounts;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleAccountListResponse {
    private String accountNo;
    private Long accountBalance;
    private String accountName;
    private Boolean authenticated;
    private Boolean isAuthCalled;

    // 우리 DB 에서 필요한 데이터는 ourAccount, 금융망 DB 에서 필요한 데이터는 record
    public static SimpleAccountListResponse toDto(InquireDemandDepositAccountListResponse.Record record, Accounts ourAccount) {
        // 우리 DB에 저장된 계좌 별칭(accountName)이 있다면 그것을 사용하고, 없다면(null이면) 어댑터에서 받은 계좌명을 예비로 사용
        String finalAccountName = (ourAccount != null) ? ourAccount.getAccountName() : record.getAccountName();



        return SimpleAccountListResponse.builder()
                .accountNo(record.getAccountNo())
                .accountBalance(record.getAccountBalance())
                .accountName(finalAccountName)
                .build();
    }

}
