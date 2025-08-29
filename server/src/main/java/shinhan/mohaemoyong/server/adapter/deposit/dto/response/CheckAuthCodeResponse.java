// adapter/deposit/dto/response/CheckAuthCodeResponse.java
package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

@Getter
public class CheckAuthCodeResponse {
    @JsonProperty("Header")
    private ResponseHeader Header;

    @JsonProperty("REC")
    private REC REC;

    @Getter
    public static class REC {
        private String status;
        private String transactionUniqueNo;
        private String accountNo;
    }
}