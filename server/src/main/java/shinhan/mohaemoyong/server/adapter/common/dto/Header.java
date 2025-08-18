// 금융 api 호출에 필요한 공통 헤더를 저장하는 DTO

package shinhan.mohaemoyong.server.adapter.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder    // 빌터패턴 사용
public class Header {
    // 공통 HEADER API 명세
    private String apiName;
    private String transmissionDate;
    private String transmissionTime;
    private String institutionCode;
    private String fintechAppNo;
    private String apiServiceCode;
    private String institutionTransactionUniqueNo;
    private String apiKey;
    private String userKey;
}
