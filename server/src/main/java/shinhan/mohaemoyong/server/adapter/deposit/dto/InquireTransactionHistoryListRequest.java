package shinhan.mohaemoyong.server.adapter.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

@Getter
@AllArgsConstructor
@ToString
public class InquireTransactionHistoryListRequest {
    @JsonProperty("Header")
    private RequestHeader header;

    private String accountNo;
    private String startDate;
    private String endDate;
    private String transactionType;
    private String orderByType;
}