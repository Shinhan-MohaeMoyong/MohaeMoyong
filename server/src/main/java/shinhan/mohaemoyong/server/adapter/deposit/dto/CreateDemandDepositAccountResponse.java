package shinhan.mohaemoyong.server.adapter.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

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
}