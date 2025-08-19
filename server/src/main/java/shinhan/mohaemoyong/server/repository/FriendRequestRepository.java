package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinhan.mohaemoyong.server.domain.FriendRequest;
import shinhan.mohaemoyong.server.domain.User;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsByRequesterAndReceiverAndStatus(User requester, User receiver, FriendRequest.Status status);
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.Status status);
}
