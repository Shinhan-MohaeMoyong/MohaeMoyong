package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shinhan.mohaemoyong.server.dto.SearchAccountResponseDto;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.AccountService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/savingState")
    public ResponseEntity<List<SearchAccountResponseDto>> getSavingState(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        // ✨ userPrincipal.getId() 대신 userPrincipal 객체 자체를 전달
        List<SearchAccountResponseDto> response = accountService.getSavingStateList(userPrincipal);
        return ResponseEntity.ok(response);
    }
}