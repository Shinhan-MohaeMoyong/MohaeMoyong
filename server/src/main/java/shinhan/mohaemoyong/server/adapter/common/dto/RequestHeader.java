package shinhan.mohaemoyong.server.adapter.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

// 요청 전용 헤더 자식 클래스
@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)  // Json 변환시 null인 필드는 제외
public class RequestHeader extends BaseHeader { // BaseHeader 상속
    private String apiKey;
    private String userKey;
}