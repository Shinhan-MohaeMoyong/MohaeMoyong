package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CreateDemandDepositAccountResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireTransactionHistoryListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.UpdateDemandDepositAccountTransferResponse;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.AccountCreateRequest;
import shinhan.mohaemoyong.server.dto.SearchAccountResponseDto;
import shinhan.mohaemoyong.server.dto.WeeklySavingDto;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.AccountRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;
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
@Service @Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountsRepository;
    private final UserRepository userRepository;
    private final DemandDepositApiAdapter demandDepositApiAdapter;
    private final PlanRepository planRepository;

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

            // 4-1. 월별 저축액 계산 (수정된 메서드 호출)
            List<WeeklySavingDto> dailySavings = getDailySavingsForMonth(user.getUserkey(), account.getAccountNumber());

            // 4-2. 달성률 계산
            double achievementRate = (targetAmount == null || targetAmount == 0) ? 0.0 : ((double) currentBalance / targetAmount) * 100.0;

            // 4-3. 최종 DTO 빌드
            return SearchAccountResponseDto.builder()
                    .accountNumber(account.getAccountNumber())
                    .balance(currentBalance)
                    .accountAlias(account.getAccountName())
                    .monthlySavings(dailySavings) // 필드명은 monthlySavings를 그대로 사용
                    .achievementRate(achievementRate)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Helper Method: 특정 계좌의 거래 내역 API를 호출하여 주차별 입금액을 계산
     */
    /**
     * Helper Method: 특정 계좌의 거래 내역 API를 호출하여 이번 달의 일별 입금액 리스트를 반환
     */
    private List<WeeklySavingDto> getDailySavingsForMonth(String userKey, String accountNumber) {
        LocalDate today = LocalDate.now();
        // 조회 기간을 이번 달 1일부터 오늘까지로 설정
        String startDate = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        InquireTransactionHistoryListRequestDto requestDto = new InquireTransactionHistoryListRequestDto(
                accountNumber, startDate, endDate, "M", "ASC" // M: 입금만 조회, ASC: 오름차순
        );

        InquireTransactionHistoryListResponse historyResponse = demandDepositApiAdapter.inquireTransactionHistoryList(userKey, requestDto);

        // API 응답이 없거나 거래 내역 리스트가 비어있으면 빈 리스트 반환
        if (historyResponse.getREC() == null || historyResponse.getREC().getList() == null) {
            return new ArrayList<>();
        }

        // 거래 내역 리스트를 DailySavingDto 리스트로 변환
        // API에서 이미 오름차순으로 정렬된 데이터를 주므로, 별도의 정렬은 필요 없음
        return historyResponse.getREC().getList().stream()
                .map(transaction -> new WeeklySavingDto(
                        transaction.getTransactionDate(), // "weekDescription"에 YYYYMMDD 날짜를 넣음
                        transaction.getTransactionBalance() // "amount"에 거래 금액을 넣음
                ))
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

    @Transactional
    public void deposit(UserPrincipal userPrincipal, Long planId) {
        Plans plans = planRepository.findById(planId).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 일정입니다."));

        String userkey = userPrincipal.getUserkey();

        String depositAccountNo = plans.getDepositAccountNo();
        String withdrawAccountNo = plans.getWithdrawAccountNo();
        Long balance = Long.valueOf(plans.getSavingsAmount());
        String summary = plans.getTitle();

        try {
            // 1. API를 통한 계좌 이체를 시도합니다.
            demandDepositApiAdapter.transfer(userkey, withdrawAccountNo, depositAccountNo, balance, summary);

            // 2. 이체가 성공했을 경우에만 약속 완료 처리를 합니다.
            plans.isCompletedUpdate();

        } catch (ApiErrorException e) {
            // 3. 어댑터가 던진 ApiErrorException을 여기서 잡습니다.
            // 서비스 레벨에서 필요한 처리를 합니다 (예: 실패 로그 기록).
            log.warn("ID {} 약속의 자동이체 실패 (API 오류): 코드 [{}], 메시지 [{}]",
                    planId, e.getErrorCode(), e.getErrorMessage());

            // 4. 잡은 예외를 다시 던져서 트랜잭션을 롤백시키고,
            //    컨트롤러가 이 예외를 받아 클라이언트에게 적절한 에러 응답을 보내도록 합니다.
            throw e;
        }
    }
}