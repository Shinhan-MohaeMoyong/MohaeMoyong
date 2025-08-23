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
    public ResponseEntity<?> deposit(@CurrentUser UserPrincipal userPrincipal,
                                     @PathVariable("planId") Long planId) {
        try {
            // 1. 서비스 로직을 호출합니다.
            accountService.deposit(userPrincipal, planId);

            // 2. 예외가 발생하지 않으면, 성공 응답(200 OK)을 반환합니다.
            return new ResponseEntity<>("자동 이체가 성공적으로 완료되었습니다.", HttpStatus.OK);

        } catch (ApiErrorException e) {
            // 3. 서비스에서 던진 ApiErrorException을 여기서 잡습니다.
            log.warn("클라이언트 요청 처리 실패: 코드 [{}], 메시지 [{}]", e.getErrorCode(), e.getErrorMessage());

            // 4. 응답 본문에 담을 에러 DTO를 생성합니다.
            ExceptionResponseDto errorResponse = ExceptionResponseDto.builder()
                    .errorCode(e.getErrorCode())
                    .errorMessage(e.getErrorMessage())
                    .build();

            // 5. 에러 코드에 따라 적절한 HTTP 상태를 결정합니다. (예: 잔액부족은 400)
            HttpStatus status = "A1014".equals(e.getErrorCode())
                    ? HttpStatus.BAD_REQUEST // 400 Bad Request
                    : HttpStatus.INTERNAL_SERVER_ERROR; // 그 외 API 에러는 500

            // 6. 상태 코드와 에러 DTO를 담아 ResponseEntity를 반환합니다.
            return new ResponseEntity<>(errorResponse, status);
        }
    }
}