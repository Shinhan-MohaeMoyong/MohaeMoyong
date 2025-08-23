package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CreateDemandDepositAccountResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireTransactionHistoryListResponse;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.AccountCreateRequest;
import shinhan.mohaemoyong.server.dto.SearchAccountResponseDto;
import shinhan.mohaemoyong.server.dto.WeeklySavingDto;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.AccountRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;
import shinhan.mohaemoyong.server.service.financedto.InquireTransactionHistoryListRequestDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 계좌 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountsRepository;
    private final UserRepository userRepository;
    private final DemandDepositApiAdapter demandDepositApiAdapter;

    /**
     * 사용자의 모든 계좌에 대한 저축 현황을 조회합니다.
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 각 계좌별 저축 현황 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<SearchAccountResponseDto> getSavingStateList(UserPrincipal userPrincipal) {
        // 1. userPrincipal 객체에서 userId 추출
        Long userId = userPrincipal.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. DB에서 해당 사용자의 모든 계좌 목록 조회
        List<Accounts> userAccounts = accountsRepository.findAllByUser(user);

        // 3. (효율성을 위해) 금융 API로 모든 계좌의 현재 잔액을 한번에 조회
        InquireDemandDepositAccountListResponse accountListResponse = demandDepositApiAdapter.inquireDemandDepositAccountList(user.getUserkey());

        Map<String, Long> balanceMap = accountListResponse.getREC().stream()
                .collect(Collectors.toMap(InquireDemandDepositAccountListResponse.Record::getAccountNo, InquireDemandDepositAccountListResponse.Record::getAccountBalance));

        // 4. 각 계좌별로 순회하며 최종 응답 DTO 리스트 생성
        return userAccounts.stream().map(account -> {
            Long currentBalance = balanceMap.getOrDefault(account.getAccountNumber(), 0L);
            Long targetAmount = account.getTargetAmount();

            // 4-1. 월별 저축액 계산 (금융 API 호출)
            List<WeeklySavingDto> weeklySavings = calculateWeeklySavings(user.getUserkey(), account.getAccountNumber());

            // 4-2. 달성률 계산
            double achievementRate = (targetAmount == null || targetAmount == 0) ? 0.0 : ((double) currentBalance / targetAmount) * 100.0;

            // 4-3. 최종 DTO 빌드
            return SearchAccountResponseDto.builder()
                    .accountNumber(account.getAccountNumber())
                    .balance(currentBalance)
                    .accountAlias(account.getAccountName())
                    .monthlySavings(weeklySavings)
                    .achievementRate(achievementRate)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Helper Method: 특정 계좌의 거래 내역 API를 호출하여 주차별 입금액을 계산
     */
    private List<WeeklySavingDto> calculateWeeklySavings(String userKey, String accountNumber) {
        LocalDate today = LocalDate.now();
        String startDate = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        InquireTransactionHistoryListRequestDto requestDto = new InquireTransactionHistoryListRequestDto(
                accountNumber, startDate, endDate, "M", "ASC" // M: 입금만 조회
        );

        InquireTransactionHistoryListResponse historyResponse = demandDepositApiAdapter.inquireTransactionHistoryList(userKey, requestDto);

        if (historyResponse.getREC() == null || historyResponse.getREC().getList() == null) {
            return new ArrayList<>();
        }

        Map<String, Long> weeklySum = historyResponse.getREC().getList().stream()
                .collect(Collectors.groupingBy(
                        transaction -> getWeekOfMonth(transaction.getTransactionDate()),
                        Collectors.summingLong(InquireTransactionHistoryListResponse.Transaction::getTransactionBalance)
                ));

        return weeklySum.entrySet().stream()
                .map(entry -> new WeeklySavingDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Helper Method: YYYYMMDD 형식의 날짜를 "M월 N주차" 형식으로 변환
     */
    private String getWeekOfMonth(String yyyymmdd) {
        LocalDate date = LocalDate.parse(yyyymmdd, DateTimeFormatter.ofPattern("yyyyMMdd"));
        int month = date.getMonthValue();
        WeekFields weekFields = WeekFields.of(DayOfWeek.SUNDAY, 1);
        int weekOfMonth = date.get(weekFields.weekOfMonth());
        return String.format("%d월 %d주", month, weekOfMonth);
    }

    @Transactional
    public void createAccount(UserPrincipal userPrincipal, AccountCreateRequest request) {
        // 0. userKey로 User 엔티티를 조회 (DB 저장을 위해 필요)
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String userKey = userPrincipal.getUserkey();


        String productUniqueNo = request.getAccountTypeUniqueNo();

        // 3. 싸피 금융 API를 호출하여 계좌를 실제로 생성합니다.
        CreateDemandDepositAccountResponse createResponse = demandDepositApiAdapter.createDemandDepositAccount(userKey, productUniqueNo);

        // 이거는 사용자가 입력한 계좌별칭
        String customAccountName = request.getAccountName();

        // 이건 사용자가 입력한 목표 저축금액
        Long customTargetAmount = request.getTargetAmount();

        Accounts newAccount = createResponse.toEntity(user, customAccountName, customTargetAmount);

        // 5. 생성된 엔티티를 우리 DB에 저장합니다.
        accountsRepository.save(newAccount);
    }
}