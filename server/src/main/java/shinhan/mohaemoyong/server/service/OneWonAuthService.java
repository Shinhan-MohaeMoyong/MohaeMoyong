package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.AccountNoRequest;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.AccountRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OneWonAuthService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public void oneWonAuthCall(UserPrincipal userPrincipal, AccountNoRequest request) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다."));
        String accountNo = request.getAccountNo();



        // 1원 송금 요청 어댑터를 성공적으로 보냈을때
        Accounts accounts = accountRepository.findByAccountNumberAndUser(accountNo, user).orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 계좌입니다."));
        accounts.updateIsAuthCalled();
    }

    @Transactional
    public void oneWonAuth(UserPrincipal userPrincipal, AccountNoRequest request) {
        Long userId = userPrincipal.getId();
        String accountNo = request.getAccountNo();
        String authCode = request.getAccountNo();


    }
}
