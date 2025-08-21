package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateDemandDepositAccountTransferResponse {
    @JsonProperty("Header")
    private ResponseHeader header; // 공통 헤더

    @JsonProperty("REC")
    private List<Record> REC; // 거래 목록

    @Getter
    @NoArgsConstructor
    public static class Record {
        // 응답 메시지 명세
        private Long transactionUniqueNo;
        private String accountNo;
        private String transactionDate;
        private String transactionType;
        private String transactionTypeName;
        private String transactionAccountNo;
    }
}