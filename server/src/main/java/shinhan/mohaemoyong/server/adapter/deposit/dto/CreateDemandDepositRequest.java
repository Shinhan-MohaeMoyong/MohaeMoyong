package shinhan.mohaemoyong.server.adapter.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.common.dto.Header;

// 전체 요청을 감싸는 클래스
@Getter
@AllArgsConstructor
public class CreateDemandDepositRequest {
    private Header Header; // 공통 헤더
    private String bankCode;
    private String accountName;
    private String accountDescription;
}