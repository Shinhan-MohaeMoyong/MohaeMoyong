package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.domain.Plans;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "plan_photos")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlanPhotos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_photo_id")
    private Long planPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plans plan;

    @Column(name = "photo_url", nullable = false, length = 512) // 255 초과
    private String photoUrl;

    @Column(name = "order_no")
    private Integer orderNo;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 내부 전용
    void setPlanInternal(Plans plan) { this.plan = plan; }

    @Builder
    private PlanPhotos(Plans plan, String photoUrl, Integer orderNo, Integer width, Integer height) {
        this.plan = plan;
        this.photoUrl = photoUrl;
        this.orderNo = (orderNo != null) ? orderNo : 0;
        this.width = width;
        this.height = height;
    }
    public static PlanPhotos create(Plans plan, String url, Integer orderNo, Integer width, Integer height) {
        // ✅ 빌더 대신 명시적 생성자 호출 (plan 절대 null 아님 보장)
        return new PlanPhotos(Objects.requireNonNull(plan, "plan"),
                Objects.requireNonNull(url, "url"),
                orderNo, width, height);
    }

    public void updateAttributes(String url, Integer orderNo, Integer width, Integer height) {
        if (url != null && !url.isBlank()) this.photoUrl = url;
        if (orderNo != null) this.orderNo = orderNo;
        if (width != null) this.width = width;
        if (height != null) this.height = height;
    }
}
