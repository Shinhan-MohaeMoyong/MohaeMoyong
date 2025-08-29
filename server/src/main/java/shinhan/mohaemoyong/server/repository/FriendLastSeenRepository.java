package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinhan.mohaemoyong.server.domain.FriendLastSeen;

import java.util.Optional;

public interface FriendLastSeenRepository extends JpaRepository<FriendLastSeen, Long> {

    Optional<FriendLastSeen> findFirstByUserIdAndFriendIdOrderByIdDesc(Long userId, Long friendId);

}
