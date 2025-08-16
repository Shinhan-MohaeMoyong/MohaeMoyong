package shinhan.mohaemoyong.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.oauth2.AuthProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_userkey", columnNames = "userkey")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 고유키 (스키마 요구: not null, unique) */
    @Column(nullable = false, length = 255)
    private String userkey;

    @Column(nullable = false, length = 255)
    private String name;

    @Email
    @Column(nullable = false, length = 255)
    private String email;

    /** 스키마에서 512 초과 길이 유지 */
    @Column(length = 512)
    private String imageUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private Boolean emailVerified = false;


    /** 생성/수정 시각 (Hibernate Timestamp) */
    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /* ===========================
       양방향 연관관계 (부모 → 자식)
       부모(User) 삭제 시 자식도 모두 삭제되도록
       cascade = ALL, orphanRemoval = true
       =========================== */

    // User(1) ↔ Plans(N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plans> plans = new ArrayList<>();

    // User(1) ↔ PlanParticipants(N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanParticipants> planParticipants = new ArrayList<>();

    // User(1) ↔ Comments(N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    // User(1) ↔ Accounts(N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Accounts> accounts = new ArrayList<>();

    /* ===========================
       연관관계 편의 메서드 (선택)
       (User는 @Setter 허용이지만,
        그래도 양방향 동기화를 보장하려면 사용 권장)
       =========================== */

    void addPlan(Plans plan) {
        plans.add(plan);
        if (plan.getUser() != this) plan.setUserInternal(this);
    }

    void addParticipant(PlanParticipants pp) {
        planParticipants.add(pp);
        if (pp.getUser() != this) pp.setUserInternal(this);
    }

    void addComment(Comments c) {
        comments.add(c);
        if (c.getUser() != this) c.setUserInternal(this);
    }

    void addAccount(Accounts a) {
        accounts.add(a);
        if (a.getUser() != this) a.setUserInternal(this);
    }
}
