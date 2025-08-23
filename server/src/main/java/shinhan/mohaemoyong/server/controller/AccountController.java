package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.dto.AccountCreateRequest;
import shinhan.mohaemoyong.server.dto.SearchAccountResponseDto;
import shinhan.mohaemoyong.server.dto.SimpleAccountListResponse;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.AccountListServive;
import shinhan.mohaemoyong.server.service.AccountService;
import java.util.List;

@RestController
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

}