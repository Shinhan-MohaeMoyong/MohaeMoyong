package shinhan.mohaemoyong.server.controller;

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
    public ResponseEntity<?> createAccount (@CurrentUser UserPrincipal userPrincipal, @RequestBody AccountCreateRequest request) {

        accountService.createAccount(userPrincipal, request);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @GetMapping("/{accountNo}")
    public ResponseEntity<AccountDetailResponse> getAccountDetails(@CurrentUser UserPrincipal userPrincipal, @PathVariable("accountNo") String accountNo) {
        AccountDetailResponse response = accountListServive.getAccountDetails(userPrincipal, accountNo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/deposit/{planId}")
    public ResponseEntity<String> deposit(@CurrentUser UserPrincipal userPrincipal, @PathVariable("planId") Long planId, @RequestBody DepositRequest request) {
        // try-catch 없이 서비스 로직만 호출
        accountService.deposit(userPrincipal, planId, request);
        return new ResponseEntity<>("자동 이체가 성공적으로 완료되었습니다.", HttpStatus.OK);
    }

    @PatchMapping("/{accountNo}/targetAmount")
    public ResponseEntity<?> updateTargetAmount(@CurrentUser UserPrincipal userPrincipal, @PathVariable("accountNo") String accountNo,
                                                                    @RequestBody AccountUpdateRequest request) {
        accountService.updateTargetAmount(userPrincipal, accountNo, request);
        return new ResponseEntity<>("수정완료", HttpStatus.OK);
    }

    @PatchMapping("/{accountNo}/accountAlias")
    public ResponseEntity<?> updateAlias(@CurrentUser UserPrincipal userPrincipal, @PathVariable("accountNo") String accountNo,
                                                             @RequestBody AccountUpdateRequest request) {
        accountService.updateAlias(userPrincipal, accountNo, request);
        return new ResponseEntity<>("수정완료", HttpStatus.OK);
    }

}