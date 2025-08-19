package shinhan.mohaemoyong.server.adapter.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// 공통 필드를 담는 부모 클래스
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseHeader {
    // 요청과 응답에 모두 포함되는 필드들
    private String apiName;
    private String transmissionDate;
    private String transmissionTime;
    private String institutionCode;
    private String fintechAppNo;
    private String apiServiceCode;
    private String institutionTransactionUniqueNo;
}