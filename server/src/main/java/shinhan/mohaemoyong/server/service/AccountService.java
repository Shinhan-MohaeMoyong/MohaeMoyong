package shinhan.mohaemoyong.server.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.*;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.*;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.AccountRepository;
import shinhan.mohaemoyong.server.repository.PlanRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;
import shinhan.mohaemoyong.server.service.financedto.InquireTransactionHistoryListRequestDto;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
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
    private final OneWonAuthService oneWonAuthService;

    // 세션에 저장할 임시 정보 객체의 키로 사용할 상수
    private static final String PENDING_ACCOUNT_INFO = "pendingAccountInfo";


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
        return userAccounts.stream()
                .filter(account -> balanceMap.containsKey(account.getAccountNumber())) // API 응답에 존재하는 계좌만 필터링
                .map(account -> {
            Long currentBalance = balanceMap.getOrDefault(account.getAccountNumber(), 0L);
            Long targetAmount = account.getTargetAmount();

            // 4-1. 월별 저축액 계산 (수정된 메서드 호출)
            List<WeeklySavingDto> dailySavings = getDailySavingsForWeek(user.getUserkey(), account.getAccountNumber());

            // 4-2. 달성률 계산
            double achievementRate = (targetAmount == null || targetAmount == 0) ? 0.0 : ((double) currentBalance / targetAmount) * 100.0;

            // 4-3. 최종 DTO 빌드
            return SearchAccountResponseDto.builder()
                    .accountNumber(account.getAccountNumber())
                    .balance(currentBalance)
                    .accountAlias(account.getAccountName())
                    .monthlySavings(dailySavings) // 필드명은 monthlySavings를 그대로 사용
                    .achievementRate(achievementRate)
                    .targetAmount(targetAmount)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Helper Method: 특정 계좌의 거래 내역 API를 호출하여 주차별 입금액을 계산
     */
    /**
     * Helper Method: 특정 계좌의 거래 내역 API를 호출하여 이번 달의 일별 입금액 리스트를 반환
     */
    private List<WeeklySavingDto> getDailySavingsForWeek(String userKey, String accountNumber) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = today.minusDays(6).format(formatter);
        String endDate = today.format(formatter);

        InquireTransactionHistoryListRequestDto requestDto = new InquireTransactionHistoryListRequestDto(
                accountNumber, startDate, endDate, "M", "ASC"
        );

        InquireTransactionHistoryListResponse historyResponse = demandDepositApiAdapter.inquireTransactionHistoryList(userKey, requestDto);

        // 1. API 응답 결과를 날짜별 입금액 합계 Map으로 변환
        Map<String, Long> dailySumMap;
        if (historyResponse.getREC() != null && historyResponse.getREC().getList() != null) {
            dailySumMap = historyResponse.getREC().getList().stream()
                    .collect(Collectors.groupingBy(
                            InquireTransactionHistoryListResponse.Transaction::getTransactionDate,
                            Collectors.summingLong(InquireTransactionHistoryListResponse.Transaction::getTransactionBalance)
                    ));
        } else {
            dailySumMap = new HashMap<>(); // 비어있는 맵 생성
        }

        // 2. 최근 7일간의 날짜 리스트를 생성
        List<WeeklySavingDto> weeklySavings = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String yyyymmdd = date.format(formatter);

            // 3. 해당 날짜의 입금액을 Map에서 조회 (없으면 0L)
            Long amount = dailySumMap.getOrDefault(yyyymmdd, 0L);

            weeklySavings.add(new WeeklySavingDto(yyyymmdd, amount));
        }

        return weeklySavings;
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
    public void createAccount(UserPrincipal userPrincipal, AccountCreateRequest request, HttpSession session) {

        try {
            oneWonAuthService.oneWonAuthCall(userPrincipal, request.getAccountNo());
        } catch (ApiErrorException e) {
            throw e;
        }

        // 2. 계좌 생성에 필요한 정보를 DTO 객체로 만들어 세션에 저장
        //    (주의: User 엔티티 같은 영속 객체를 세션에 직접 저장하는 것은 피해야 합니다)
        PendingAccountInfo pendingInfo = new PendingAccountInfo(
                request.getAccountNo(),
                request.getAccountName(),
                request.getTargetAmount(),
                request.getAccountTypeUniqueNo()
        );
        session.setAttribute(PENDING_ACCOUNT_INFO, pendingInfo);

        // 세션 유효 시간 설정 (10분)
        session.setMaxInactiveInterval(60 * 10);
    }

    /**
     * 2단계: 1원 인증 코드 검증 및 계좌 생성
     */
    @Transactional
    public void verifyAndCreateAccount(UserPrincipal userPrincipal, AccountVerificationRequest request, HttpSession session) {
        // 1. 세션에서 임시 저장된 계좌 정보를 가져옴
        PendingAccountInfo pendingInfo = (PendingAccountInfo) session.getAttribute(PENDING_ACCOUNT_INFO);

        // 세션 정보가 없거나, 요청된 계좌번호와 다를 경우 비정상적인 접근으로 처리
        if (pendingInfo == null || !pendingInfo.getAccountNo().equals(request.getAccountNo())) {
            throw new IllegalStateException("인증 정보가 만료되었거나 올바르지 않습니다. 처음부터 다시 시도해주세요.");
        }

        try {
            // 2. 1원 인증 코드 검증 (실패 시 예외 발생)
            oneWonAuthService.oneWonAuth(userPrincipal, request.getAccountNo(), request.getAuthCode());
        } catch (ApiErrorException e) {
            throw e;
        }

        // 이제부터 저장 로직 (우리DB, 금융망DB 둘다)

        // 3. User 엔티티 조회
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 4. 금융망 DB에 계좌 생성 (세션에서 가져온 정보 사용)
        CreateDemandDepositAccountResponse createResponse = demandDepositApiAdapter.createDemandDepositAccount(
                user.getUserkey(),
                pendingInfo.getAccountTypeUniqueNo()
        );

        // 5. 우리 DB에도 계좌 정보 저장 (세션에서 가져온 정보 사용)
        Accounts newAccount = createResponse.toEntity(
                user,
                pendingInfo.getCustomAccountName(),
                pendingInfo.getCustomTargetAmount()
        );

        accountsRepository.save(newAccount);

        // 6. 처리가 완료되었으므로 세션에서 임시 정보 제거
        session.removeAttribute(PENDING_ACCOUNT_INFO);
    }

    @Transactional
    public void fetchAccount(UserPrincipal userPrincipal, AccountCreateRequest request, HttpSession session) {

        try {
            oneWonAuthService.oneWonAuthCall(userPrincipal, request.getAccountNo());
        } catch (ApiErrorException e) {
            throw e;
        }

        // 2. 계좌 생성에 필요한 정보를 DTO 객체로 만들어 세션에 저장
        //    (주의: User 엔티티 같은 영속 객체를 세션에 직접 저장하는 것은 피해야 합니다)
        PendingAccountInfo pendingInfo = new PendingAccountInfo(
                request.getAccountNo()
        );
        session.setAttribute(PENDING_ACCOUNT_INFO, pendingInfo);

        // 세션 유효 시간 설정 (10분)
        session.setMaxInactiveInterval(60 * 10);
    }

    /**
     * 2단계: 1원 인증 코드 검증 및 계좌 생성
     */
    @Transactional
    public void verifyAndFetchAccount(UserPrincipal userPrincipal, AccountVerificationRequest request, HttpSession session) {
        // 1. 세션에서 임시 저장된 계좌 정보를 가져옴
        PendingAccountInfo pendingInfo = (PendingAccountInfo) session.getAttribute(PENDING_ACCOUNT_INFO);

        // 세션 정보가 없는경우 비정상적인 접근으로 처리
        if (pendingInfo == null) {
            throw new IllegalStateException("인증 정보가 만료되었거나 올바르지 않습니다. 처음부터 다시 시도해주세요.");
        }

        try {
            // 2. 1원 인증 코드 검증 (실패 시 예외 발생)
            oneWonAuthService.oneWonAuth(userPrincipal, request.getAccountNo(), request.getAuthCode());
        } catch (ApiErrorException e) {
            throw e;
        }

        // 이제부터 저장 로직 (우리DB)

        // 3. User 엔티티 조회
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 4. 금융망 DB에 계좌 생성 (세션에서 가져온 정보 사용)
        InquireDemandDepositAccountResponse response = demandDepositApiAdapter.inquireDemandDepositAccount(
                user.getUserkey(),
                pendingInfo.getAccountNo()
        );

        // 5. 우리 DB에도 계좌 정보 저장 (세션에서 가져온 정보 사용)
        Accounts newAccount = response.toEntity(user, response);

        accountsRepository.save(newAccount);

        // 6. 처리가 완료되었으므로 세션에서 임시 정보 제거
        session.removeAttribute(PENDING_ACCOUNT_INFO);
    }

    @Getter
    @AllArgsConstructor @NoArgsConstructor
    private static class PendingAccountInfo implements Serializable {
        private String accountNo;
        private String customAccountName;
        private Long customTargetAmount;
        private String accountTypeUniqueNo;

        public PendingAccountInfo(String accountNo) {
            this.accountNo = accountNo;
        }
    }

    @Transactional
    public void deposit(UserPrincipal userPrincipal, Long planId, DepositRequest request) {
        Plans plans = planRepository.findById(planId).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 일정입니다."));

        String userkey = userPrincipal.getUserkey();

        String depositAccountNo = plans.getDepositAccountNo();
        Long balance = Long.valueOf(plans.getSavingsAmount());
        String summary = plans.getTitle();

        // 사용자로 부터 입력받은 출금 계좌 번호
        String withdrawAccountNo = request.getWithdrawAccountNo();

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

    @Transactional
    public void updateTargetAmount(UserPrincipal userPrincipal, AccountUpdateRequest request) {
        Long userId = userPrincipal.getId();

        Optional<Accounts> accounts0 = accountsRepository.findByAccountNumber(request.getAccountNo());

        Accounts account = accounts0.orElseThrow(() ->
                new EntityNotFoundException("해당 계좌 번호를 찾을 수 없습니다: " + request.getAccountNo())
        );

        if (Objects.equals(account.getUser().getId(), userId)) {
            account.updateTargetAmount(request.getTargetAmount()); // 변경 감지
        } else { // 현재 로그인된 사용자가 해당 계좌를 등록한 사용자가 아님
            throw new ApiErrorException("E001","권한이 없습니다.");
        }

    }

    @Transactional
    public void updateAlias(UserPrincipal userPrincipal, AccountUpdateRequest request) {
        Long userId = userPrincipal.getId();

        Optional<Accounts> accounts0 = accountsRepository.findByAccountNumber(request.getAccountNo());

        Accounts account = accounts0.orElseThrow(() ->
                new EntityNotFoundException("해당 계좌 번호를 찾을 수 없습니다: " + request.getAccountNo())
        );

        if (Objects.equals(account.getUser().getId(), userId)) {
            account.updateAlias(request.getAccountAlias()); // 변경 감지
        } else { // 현재 로그인된 사용자가 해당 계좌를 등록한 사용자가 아님
            throw new ApiErrorException("E001","권한이 없습니다.");
        }
    }
}
