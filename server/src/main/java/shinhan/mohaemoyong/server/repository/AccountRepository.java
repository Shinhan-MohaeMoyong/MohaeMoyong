package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.domain.User;

import java.util.List;

public interface AccountRepository extends JpaRepository<Accounts, Long> {

    /**
     * 특정 사용자가 소유한 모든 계좌를 조회합니다.
     * @param user 조회할 User 엔티티
     * @return 해당 사용자의 Accounts 목록
     */
    List<Accounts> findAllByUser(User user);
}