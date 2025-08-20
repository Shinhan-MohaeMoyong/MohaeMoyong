package shinhan.mohaemoyong.server.adapter.common.headerDto;


import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 응답 전용 헤더 자식 클래스
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseHeader extends BaseHeader { // BaseHeader 상속
    // 응답에만 필요한 필드들
    private String responseCode;    // 응답코드
    private String responseMessage; // 응답메세지
}