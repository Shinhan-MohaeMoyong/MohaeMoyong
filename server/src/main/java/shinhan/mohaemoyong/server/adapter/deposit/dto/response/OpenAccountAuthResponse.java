package shinhan.mohaemoyong.server.adapter.deposit.dto.response;

import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

@Getter
public class OpenAccountAuthResponse {
    private ResponseHeader Header;
    // openAccountAuth API는 성공 시 별도의 데이터 필드가 없으므로 헤더만 정의합니다.
}