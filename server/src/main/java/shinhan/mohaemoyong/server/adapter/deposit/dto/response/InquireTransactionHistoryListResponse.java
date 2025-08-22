package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

import java.util.List;

@Getter
@NoArgsConstructor
public class InquireTransactionHistoryListResponse {
    @JsonProperty("Header")
    private ResponseHeader header;

    @JsonProperty("REC")
    private Record REC;

    @Getter
    @NoArgsConstructor
    public static class Record {
        private String totalCount;
        private List<Transaction> list;
    }

    @Getter
    @NoArgsConstructor
    public static class Transaction {
        private Long transactionUniqueNo;
        private String transactionDate;
        private String transactionTime;
        private String transactionType;
        private String transactionTypeName;
        private String transactionAccountNo;
        private Long transactionBalance;
        private Long transactionAfterBalance;
        private String transactionSummary;
        private String transactionMemo;
    }
}