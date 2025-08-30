package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;

@Getter
@NoArgsConstructor
public class InquireDemandDepositAccountResponse {

    @JsonProperty("Header")
    private ResponseHeader header;

    @JsonProperty("REC")
    private Record REC;

    @Getter
    @NoArgsConstructor
    public static class Record {
        @JsonProperty("bankCode")
        private String bankCode;

        @JsonProperty("bankName")
        private String bankName;

        @JsonProperty("userName")
        private String userName;

        @JsonProperty("accountNo")
        private String accountNo;

        @JsonProperty("accountName")
        private String accountName;

        @JsonProperty("accountTypeCode")
        private String accountTypeCode;

        @JsonProperty("accountTypeName")
        private String accountTypeName;

        @JsonProperty("accountCreatedDate")
        private String accountCreatedDate;

        @JsonProperty("accountExpiryDate")
        private String accountExpiryDate;

        @JsonProperty("dailyTransferLimit")
        private String dailyTransferLimit;

        @JsonProperty("oneTimeTransferLimit")
        private String oneTimeTransferLimit;

        @JsonProperty("accountBalance")
        private String accountBalance;

        @JsonProperty("lastTransactionDate")
        private String lastTransactionDate;

        @JsonProperty("currency")
        private String currency;
    }

    /**
     * API 응답 정보와 User 엔티티를 조합하여 새로운 Accounts 엔티티를 생성합니다.
     * @param user 이 계좌를 소유할 User 엔티티
     * @param response API 응답 객체
     * @return 생성된 Accounts 엔티티
     */
    public Accounts toEntity(User user, InquireDemandDepositAccountResponse response) {
        return Accounts.builder()
                .user(user)
                .username(user.getName()) // User 엔티티에서 사용자 실명 가져오기
                .accountName(response.getREC().getAccountName()) // 파라미터로 받은 계좌 별칭
                .accountNumber(response.getREC().getAccountNo()) // API 응답으로 받은 계좌번호
                .bankCode(response.getREC().getBankCode()) // API 응답으로 받은 은행 코드
                .bankName("신한은행")
                .targetAmount(100000L) // 파라미터로 받은 목표 저축금액
                .build();
    }
}

