package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
@Builder
@Entity
@Table(name = "accounts")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    // 대표(소유자), FK to users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "account_number", length = 255)
    private String accountNumber;

    @Column(name = "bank_code", length = 255)
    private String bankCode;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "account_expiry_date")
    private LocalDate accountExpiryDate;

    @Column(name = "target_amount")
    private Long targetAmount; // ✨ 목표 금액 필드 추가, 달성률을 나타내기 위함, 조현우

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // columnDefinition : 기존에 DB에 저장되어 있던 데이터나, 애플리케이션을 거치지 않고 DB에서 직접 추가된 데이터에 대해서는 기본값을 보장
    @Column(name = "authenticated", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean authenticated = false;

    // 내부 전용
    void setUserInternal(User user) { this.user = user; }

    public void updateTargetAmount(Long targetAmount) { this.targetAmount = targetAmount; }

    public void updateAlias(String alias) { this.accountName = alias; }
}
