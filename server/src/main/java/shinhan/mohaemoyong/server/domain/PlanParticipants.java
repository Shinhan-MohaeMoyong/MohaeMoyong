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
                @UniqueConstraint(name = "uk_plan_participants_plan_user", columnNames = {"plan_id", "id"})
        }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanParticipants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id") // 실제 테이블에 추가되는 PK 컬럼
    private Long participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plans plan;

    // users.id 컬럼명이 'id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    // ✅ 변경된 부분
    @Enumerated(EnumType.STRING)  // DB에 문자열 OWNER / MEMBER 로 저장
    @Column(name = "role", nullable = false, length = 20)
    private ParticipantRole role;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 내부 전용
    void setPlanInternal(Plans plan) { this.plan = plan; }
    void setUserInternal(User user) { this.user = user; }
}
