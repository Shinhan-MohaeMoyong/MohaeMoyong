package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.dto.AccountNoRequest;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.OneWonAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class OneWonAuthController {
    private final OneWonAuthService oneWonAuthService;

    @PostMapping("/api/v1/account/auth/call")
    public ResponseEntity<?> oneWonAuthCall(@CurrentUser UserPrincipal userPrincipal,
                                            @RequestBody AccountNoRequest request) {
        oneWonAuthService.oneWonAuthCall(userPrincipal, request);
        return new ResponseEntity<>("해당 계좌로 1원을 송금하였씁니다, 거래 내역에서 인증코드를 확인하시길 바랍니다.", HttpStatus.ACCEPTED);
    }

    @PostMapping("/api/v1/account/auth")
    public ResponseEntity<?> oneWonAuth(@CurrentUser UserPrincipal userPrincipal,
                                        @RequestBody AccountNoRequest request) {
        oneWonAuthService.oneWonAuth(userPrincipal, request);
        return new ResponseEntity<>("인증에 성공하였습니다.", HttpStatus.OK);
    }
}
