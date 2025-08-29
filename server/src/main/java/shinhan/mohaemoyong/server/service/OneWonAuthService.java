package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CheckAuthCodeResponse;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.AccountNoRequest;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.AccountRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;


@Service @Slf4j
@RequiredArgsConstructor
public class OneWonAuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final DemandDepositApiAdapter demandDepositApiAdapter;

    @Transactional
    public void oneWonAuthCall(UserPrincipal userPrincipal, AccountNoRequest request) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다."));
        String accountNo = request.getAccountNo();

        try {
            // 2. 외부 API 어댑터 호출 (userKey 추가하여 전달)
            log.info("1원 입금 인증을 시작합니다. 계좌번호: {}", accountNo);
            demandDepositApiAdapter.oneWonAuthCall(user.getUserkey(), accountNo);

        } catch (ApiErrorException e) {
            // 3. 외부 API 호출 실패 시 예외 처리
            log.error("1원 입금 인증 API 호출에 실패했습니다. 응답 코드: {}, 메시지: {}", e.getErrorCode(), e.getErrorMessage());
            // 컨트롤러에서 처리할 수 있도록 예외를 다시 던지거나, 특정 응답 DTO를 반환할 수 있습니다.
            throw e;
        }

        // 4. API 호출 성공 후, 우리 서비스의 계좌 정보 업데이트
        Accounts accounts = accountRepository.findByAccountNumberAndUser(accountNo, user)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 계좌입니다."));
        accounts.updateIsAuthCalled(); // isAuthCalled = true;

        log.info("1원 입금 인증 요청이 성공적으로 처리되었습니다. 계좌번호: {}", accountNo);

    }

    @Transactional
    public void oneWonAuth(UserPrincipal userPrincipal, AccountNoRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다."));

        CheckAuthCodeResponse response;
        try {
            // 1. 외부 API 어댑터 호출
            response = demandDepositApiAdapter.oneWonAuth(user.getUserkey(), request.getAccountNo(), request.getAuthCode());
        } catch (ApiErrorException e) {
            // 외부 API 자체가 4xx, 5xx 에러를 반환한 경우
            log.error("1원 인증 확인 API 호출 실패. 코드: {}, 메시지: {}", e.getErrorCode(), e.getErrorMessage());
            throw e; // 예외를 그대로 상위로 전달하여 GlobalExceptionHandler에서 처리
        }

        // 2. 외부 API 응답 결과 확인
        if ("SUCCESS".equals(response.getREC().getStatus())) {
            // 3. 인증 성공 시, 우리 서비스의 계좌 정보 업데이트
            Accounts accounts = accountRepository.findByAccountNumberAndUser(request.getAccountNo(), user)
                    .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 계좌입니다."));

            accounts.updateAuthenticated(); // authenticated = true;
            log.info("계좌 인증에 성공했습니다. 계좌번호: {}", request.getAccountNo());

        } else {
            // 4. 인증 실패 시(예: status="FAIL"), 우리 서비스 명세에 맞는 예외 발생
            log.warn("인증 코드가 틀렸습니다. 계좌번호: {}", request.getAccountNo());
            throw new ApiErrorException("E002", "인증 코드가 틀렸습니다.");
        }
    }
}
