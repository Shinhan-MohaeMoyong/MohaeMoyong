package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireTransactionHistoryListResponse;
import shinhan.mohaemoyong.server.domain.Accounts;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailResponse {

    private String accountNo;
    private Long accountBalance;
    private String accountName;
    private Long targetAmount;
    private List<TransactionRecord> list;

    /**
     * DB 정보와 외부 API 응답을 조합하여 최종 DTO를 생성합니다.
     * @param ourAccount DB에서 조회한 계좌 엔티티
     * @param historyResponse 외부 API에서 받은 거래 내역 응답
     * @return 완성된 AccountDetailResponse 객체
     */
    public static AccountDetailResponse toDto(Accounts ourAccount, InquireTransactionHistoryListResponse historyResponse) {
        // API 응답에서 실제 거래 내역 리스트를 안전하게 가져옵니다.
        List<InquireTransactionHistoryListResponse.Transaction> apiTransactions =
                (historyResponse.getREC() != null && historyResponse.getREC().getList() != null)
                        ? historyResponse.getREC().getList()
                        : Collections.emptyList();

        // 최신 거래의 '거래후잔액'을 현재 잔액으로 설정합니다. (거래 내역이 없으면 0)
        Long currentBalance = apiTransactions.isEmpty() ? 0L : apiTransactions.get(0).getTransactionAfterBalance();

        // API 거래 내역을 우리 DTO 형식으로 변환합니다.
        List<TransactionRecord> transactionRecords = apiTransactions.stream()
                .map(TransactionRecord::toDto) // 내부 클래스의 toDto를 호출
                .collect(Collectors.toList());

        return AccountDetailResponse.builder()
                .accountNo(ourAccount.getAccountNumber())
                .accountBalance(currentBalance)
                .targetAmount(ourAccount.getTargetAmount())
                .accountName(ourAccount.getAccountName())
                .list(transactionRecords)
                .build();
    }


    /**
     * list 내부의 객체를 나타내는 static 중첩 클래스
     */
    @Getter
    @Builder
    public static class TransactionRecord {
        private String transactionDate;
        private String transactionTime;
        private String transactionType;
        private String transactionTypeName;
        private Long transactionBalance;
        private Long transactionAfterBalance;
        private String transactionSummary;

        /**
         * 외부 API의 Transaction 객체를 우리 DTO의 TransactionRecord로 변환합니다.
         * @param apiTransaction 외부 API의 단일 거래 내역 객체
         * @return 변환된 TransactionRecord 객체
         */
        public static TransactionRecord toDto(InquireTransactionHistoryListResponse.Transaction apiTransaction) {
            return TransactionRecord.builder()
                    .transactionDate(apiTransaction.getTransactionDate())
                    .transactionTime(apiTransaction.getTransactionTime())
                    .transactionType(apiTransaction.getTransactionType())
                    .transactionTypeName(apiTransaction.getTransactionTypeName())
                    .transactionBalance(apiTransaction.getTransactionBalance())
                    .transactionAfterBalance(apiTransaction.getTransactionAfterBalance())
                    .transactionSummary(apiTransaction.getTransactionSummary())
                    .build();
        }
    }
}