package shinhan.mohaemoyong.server.adapter.deposit.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

// 전체 요청을 감싸는 클래스
@Getter
@AllArgsConstructor
public class CreateDemandDepositRequest {
    @JsonProperty("Header") // JSON 필드명 맵핑
    private RequestHeader header;

    private String bankCode;
    private String accountName;
    private String accountDescription;
}