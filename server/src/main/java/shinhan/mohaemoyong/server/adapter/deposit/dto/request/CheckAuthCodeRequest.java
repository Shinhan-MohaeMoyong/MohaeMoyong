package shinhan.mohaemoyong.server.adapter.deposit.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

@Getter
@AllArgsConstructor
public class CheckAuthCodeRequest {

    /**
     * 공통 헤더
     */
    @JsonProperty("Header")
    private RequestHeader Header;

    /**
     * 인증할 계좌 번호
     */
    private String accountNo;

    /**
     */
    private String authText;

    /**
     * 사용자가 입력한 인증 코드
     */
    private String authCode;
}