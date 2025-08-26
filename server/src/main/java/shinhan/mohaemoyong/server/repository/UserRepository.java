package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import shinhan.mohaemoyong.server.domain.User;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("""
SELECT u
FROM User u
WHERE u.id <> :myId

AND NOT EXISTS (
    SELECT 1 FROM FriendRequest fr
    WHERE fr.requester.id = :myId
      AND fr.receiver.id = u.id
      AND fr.isActive = true
)

AND NOT EXISTS (
    SELECT 1 FROM Friendship f
    WHERE (f.user.id = :myId AND f.friend.id = u.id)
       OR (f.friend.id = :myId AND f.user.id = u.id)
)

AND (:query IS NULL OR :query = '' 
     OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))
""")
    List<User> searchAvailableUsers(@Param("myId") Long myId,
                                    @Param("query") String query);
}
