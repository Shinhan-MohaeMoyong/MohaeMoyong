package shinhan.mohaemoyong.server.adapter.deposit.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

@Getter
@AllArgsConstructor
@ToString
public class InquireDemandDepositAccountListRequest {
    @JsonProperty("Header")
    private RequestHeader header;
}