package shinhan.mohaemoyong.server.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Service 계층에서 Adapter 계층으로 거래 내역 조회 요청 시 필요한 데이터를 전달하는 DTO
 */
@Getter
@AllArgsConstructor
@ToString
public class InquireTransactionHistoryListRequestDto {
    private String accountNo;
    private String startDate;
    private String endDate;
    private String transactionType;
    private String orderByType;
}