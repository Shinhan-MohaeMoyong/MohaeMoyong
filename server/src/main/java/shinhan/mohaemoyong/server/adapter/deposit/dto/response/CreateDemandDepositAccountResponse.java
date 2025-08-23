package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;

@Getter
@NoArgsConstructor
public class CreateDemandDepositAccountResponse {
    @JsonProperty("Header")
    private ResponseHeader header;

    @JsonProperty("REC")
    private Record REC; // JSON 예시에 따라 List가 아닌 단일 객체로 정의

    @Getter
    @NoArgsConstructor
    public static class Record {
        private String bankCode;
        private String accountNo;
        private Currency currency; // 중첩된 currency 객체
    }

    @Getter
    @NoArgsConstructor
    public static class Currency {
        // JSON 키 "currency"와 필드명 "currency"가 겹치므로,
        // Java 필드명은 더 명확하게 currencyCode로 변경하고 @JsonProperty로 매핑
        @JsonProperty("currency")
        private String currency;
        private String currencyName;
    }

    public Accounts toEntity(User userParam, String customAccountName, Long customTargetAmount) {
        return Accounts.builder()
                .user(userParam)
                .username(userParam.getName()) // User 엔티티에서 사용자 실명 가져오기
                .accountName(customAccountName) // 파라미터로 받은 계좌 별칭
                .accountNumber(this.REC.getAccountNo()) // API 응답으로 받은 계좌번호
                .bankCode(this.REC.getBankCode()) // API 응답으로 받은 은행 코드
                .bankName("신한은행")
                .targetAmount(customTargetAmount) // 파라미터로 받은 목표 저축금액
                .build();
    }
}