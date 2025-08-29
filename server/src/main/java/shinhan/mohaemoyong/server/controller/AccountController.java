package shinhan.mohaemoyong.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
import shinhan.mohaemoyong.server.adapter.exception.ExceptionResponseDto;
import shinhan.mohaemoyong.server.dto.*;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.AccountListServive;
import shinhan.mohaemoyong.server.service.AccountService;
import java.util.List;

@RestController @Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;
    private final AccountListServive accountListServive;

    @GetMapping("/savingState")
    public ResponseEntity<List<SearchAccountResponseDto>> getSavingState(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        // ✨ userPrincipal.getId() 대신 userPrincipal 객체 자체를 전달
        List<SearchAccountResponseDto> response = accountService.getSavingStateList(userPrincipal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/simpleList")
    public ResponseEntity<List<SimpleAccountListResponse>> getSimpleAccountList (@CurrentUser UserPrincipal userPrincipal) {
        String userkey = userPrincipal.getUserkey();

        List<SimpleAccountListResponse> response = accountListServive.getSimpleAccountList(userkey);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createAccount (@CurrentUser UserPrincipal userPrincipal, @RequestBody AccountCreateRequest request,
                                            HttpServletRequest httpServletRequest) {

        accountService.createAccount(userPrincipal, request, httpServletRequest.getSession());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    /**
     * 계좌 생성의 2단계: 1원 인증 코드 검증 및 계좌 생성 API
     */
    @PostMapping("/auth")
    public ResponseEntity<String> verifyAndCreateAccount(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody AccountVerificationRequest request,
            HttpServletRequest httpServletRequest) { // HttpSession을 사용하기 위해 추가

        accountService.verifyAndCreateAccount(userPrincipal, request, httpServletRequest.getSession());

        return ResponseEntity.status(HttpStatus.CREATED).body("계좌 인증 및 생성이 성공적으로 완료되었습니다.");
    }


    @PostMapping("/detail")
    public ResponseEntity<AccountDetailResponse> getAccountDetails(@CurrentUser UserPrincipal userPrincipal, @RequestBody AccountNoRequest request) {
        AccountDetailResponse response = accountListServive.getAccountDetails(userPrincipal, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/deposit/{planId}")
    public ResponseEntity<String> deposit(@CurrentUser UserPrincipal userPrincipal, @PathVariable("planId") Long planId, @RequestBody DepositRequest request) {
        // try-catch 없이 서비스 로직만 호출
        accountService.deposit(userPrincipal, planId, request);
        return new ResponseEntity<>("자동 이체가 성공적으로 완료되었습니다.", HttpStatus.OK);
    }

    @PatchMapping("/targetAmount")
    public ResponseEntity<?> updateTargetAmount(@CurrentUser UserPrincipal userPrincipal, @RequestBody AccountUpdateRequest request) {
        accountService.updateTargetAmount(userPrincipal, request);
        return new ResponseEntity<>("수정완료", HttpStatus.OK);
    }

    @PatchMapping("/accountAlias")
    public ResponseEntity<?> updateAlias(@CurrentUser UserPrincipal userPrincipal, @RequestBody AccountUpdateRequest request) {
        accountService.updateAlias(userPrincipal, request);
        return new ResponseEntity<>("수정완료", HttpStatus.OK);
    }

}
