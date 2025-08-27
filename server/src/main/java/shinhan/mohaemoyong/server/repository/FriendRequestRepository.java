package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.FriendRequest;
import shinhan.mohaemoyong.server.domain.User;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsByRequesterAndReceiverAndStatus(User requester, User receiver, FriendRequest.Status status);

    // 내가 받은 요청 목록 (status로 필터링)
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.Status status);

    // 내가 보낸 요청 목록 (status로 필터링)
    List<FriendRequest> findByRequester_IdAndStatus(Long requesterId, FriendRequest.Status status);

    /** 두 사람 사이의 모든 요청 상태 변경 (취소/비활성화) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE FriendRequest fr
       SET fr.status = :status,
           fr.isActive = false
     WHERE (fr.requester.id = :a AND fr.receiver.id = :b)
        OR (fr.requester.id = :b AND fr.receiver.id = :a)
""")
    int cancelAllBetweenByIds(@Param("a") Long a,
                              @Param("b") Long b,

                              @Param("status") FriendRequest.Status status);
}
