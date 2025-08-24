package shinhan.mohaemoyong.server.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.Friendship;
import shinhan.mohaemoyong.server.domain.User;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByUser(User user);

    boolean existsByUserAndFriend(User user, User friend);
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    /** 양방향 관계 한번에 삭제 */
    @Modifying
    @Transactional
    @Query("DELETE FROM Friendship f WHERE " +
            "(f.user = :user AND f.friend = :friend) OR " +
            "(f.user = :friend AND f.friend = :user)")
    void deletePair(User user, User friend);


    @Query("""
        select (count(f) > 0) from Friendship f
         where f.user.id = :a and f.friend.id = :b
    """)
    boolean existsEdge(@Param("a") Long a, @Param("b") Long b);


    @Query("""
        select case when count(f) > 0 then true else false end
          from Friendship f
         where (f.user.id = :a and f.friend.id = :b)
            or (f.user.id = :b and f.friend.id = :a)
    """)
    boolean existsFriendEdge(@Param("a") Long a, @Param("b") Long b);
}
