package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "friend_last_seen", // 실제 테이블 이름에 맞게 확인하세요.
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"userId", "friendId"}) // 실제 필드명 확인!
        }
)
@Getter
@NoArgsConstructor
public class FriendLastSeen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 내가 누구인지 */
    @Column(nullable = false)
    private Long userId;

    /** 어떤 친구를 확인했는지 */
    @Column(nullable = false)
    private Long friendId;

    /** 마지막 확인한 시간 */
    @Column(nullable = false)
    private LocalDateTime lastSeenAt;

    public FriendLastSeen(Long userId, Long friendId, LocalDateTime lastSeenAt) {
        this.userId = userId;
        this.friendId = friendId;
        this.lastSeenAt = lastSeenAt;
    }

    public void updateLastSeen(LocalDateTime time) {
        this.lastSeenAt = time;
    }
}
