package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Accounts, Long> {

    /**
     * 특정 사용자가 소유한 모든 계좌를 조회합니다.
     * @param user 조회할 User 엔티티
     * @return 해당 사용자의 Accounts 목록
     */
    List<Accounts> findAllByUser(User user);

    /**
     * 여러 계좌번호에 해당하는 계좌 목록을 한 번의 쿼리로 조회합니다.
     * @param accountNumbers 조회할 계좌번호 리스트
     * @return 조회된 Accounts 엔티티 리스트
     */
    List<Accounts> findAllByAccountNumberIn(List<String> accountNumbers);

    Optional<Accounts> findByAccountNumber(String accountNumber);

}