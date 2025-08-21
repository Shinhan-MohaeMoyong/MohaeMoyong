package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity
@Table(
        name = "plan_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_participants_plan_user",
                        columnNames = {"plan_id", "user_id"} // ✅ 수정
                )
        }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanParticipants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plans plan;

    // ✅ FK 컬럼명 수정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role", nullable = false, length = 255)
    private String role = "member";

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    void setPlanInternal(Plans plan) { this.plan = plan; }
    void setUserInternal(User user) { this.user = user; }
}
