package shinhan.mohaemoyong.server.adapter.common.headerDto;

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
    private String apiName;                 // API 이름
    private String transmissionDate;        // 전송일자
    private String transmissionTime;        // 전송시각
    private String institutionCode;         // 기관코드
    private String fintechAppNo;            // 핀테크 앱 일련번호
    private String apiServiceCode;          // API 서비스코드
    private String institutionTransactionUniqueNo;  // 기관거래 고유번호

}