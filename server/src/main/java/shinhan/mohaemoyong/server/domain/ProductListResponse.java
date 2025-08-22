package shinhan.mohaemoyong.server.domain;

import lombok.Builder;
import lombok.Getter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositListResponse;

@Getter
@Builder
public class ProductListResponse {
    private String accountName;
    private String accountDescription;

    public static ProductListResponse toDto(InquireDemandDepositListResponse.Record response) {
        return ProductListResponse.builder()
                .accountName(response.getAccountName())
                .accountDescription(response.getAccountDescription())
                .build();
    }
}
