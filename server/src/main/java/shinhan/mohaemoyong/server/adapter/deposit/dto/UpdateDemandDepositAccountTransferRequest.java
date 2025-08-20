package shinhan.mohaemoyong.server.adapter.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

@Getter
@AllArgsConstructor
public class UpdateDemandDepositAccountTransferRequest {
    @JsonProperty("Header")
    private RequestHeader Header;   // 공통 헤더
    
    // 요청 메세지 명세
    private String depositAccountNo;
    private Long transactionBalance;
    private String withdrawalAccountNo;
    private String depositTransactionSummary;
    private String withdrawalTransactionSummary;
}
