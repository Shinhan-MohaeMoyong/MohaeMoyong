package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

import java.util.List;

@Getter
@NoArgsConstructor
public class InquireDemandDepositAccountListResponse {
    @JsonProperty("Header")
    private ResponseHeader header;

    @JsonProperty("REC")
    private List<Record> REC;

    @Getter
    @NoArgsConstructor
    public static class Record {
        // 응답 메시지 명세
        private String bankCode;
        private String bankName;
        private String username;
        private String accountNo;
        private String accountName;
        private String accountTypeCode;
        private String accountTypeName;
        private String accountCreatedDate;
        private String accountExpiryDate;
        private Long dailyTransferLimit;
        private Long oneTimeTransferLimit;
        private Long accountBalance;
        private String lastTransactionDate;
        private String currency;
    }
}