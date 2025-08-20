package shinhan.mohaemoyong.server.adapter.deposit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.common.headerDto.ResponseHeader;

import java.util.List;

@Getter
@NoArgsConstructor
public class InquireDemandDepositListResponse {
    @JsonProperty("Header")
    private ResponseHeader header;

    @JsonProperty("REC")
    private List<Record> REC;

    @Getter
    @NoArgsConstructor
    public static class Record {
        // 응답 메시지 명세
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