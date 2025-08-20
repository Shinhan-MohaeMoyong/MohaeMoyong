package shinhan.mohaemoyong.server.adapter.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

import java.util.List;

// 전체 응답을 감싸는 클래스
@Getter
@NoArgsConstructor
public class CreateDemandDepositResponse {
    @JsonProperty("Header") // JSON 필드명과 Java 필드명을 매핑
    private ResponseHeader Header;

    @JsonProperty("REC") // JSON 필드명과 Java 필드명을 매핑
    private List<Record> REC;

    // REC 필드 내부의 객체를 표현하는 중첩 클래스
    @Getter
    @NoArgsConstructor
    public static class Record {
        private String accountTypeUniqueNo;
        private String bankCode;
        private String bankName;
        private String accountTypeCode;
        private String accountTypeName;
        private String accountName;
        private String accountDescription;
        private String accountType;
    }
}