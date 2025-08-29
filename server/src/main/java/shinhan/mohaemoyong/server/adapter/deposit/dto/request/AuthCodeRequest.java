// adapter/deposit/dto/request/RequestAuthCodeRequest.java
package shinhan.mohaemoyong.server.adapter.deposit.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

@Getter
@AllArgsConstructor
public class AuthCodeRequest {

    @JsonProperty("Header")
    private RequestHeader Header;

    private String accountNo;
    private String authText;
}