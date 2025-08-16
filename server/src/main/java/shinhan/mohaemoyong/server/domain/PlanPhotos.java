package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.domain.Plans;

import java.time.Instant;

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
}
