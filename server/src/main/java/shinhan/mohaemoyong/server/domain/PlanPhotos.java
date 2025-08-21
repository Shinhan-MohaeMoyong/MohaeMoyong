package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "plan_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PlanPhotos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_photo_id")
    private Long planPhotoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private Plans plan;

    @Column(name = "photo_url", nullable = false, length = 512)
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

    /** 연관관계 세팅 - 같은 패키지에서만 사용 */
    void setPlanInternal(Plans plan) { this.plan = plan; }

    /** 팩토리 메서드 */
    public static PlanPhotos of(String url, Integer order, Integer width, Integer height) {
        return PlanPhotos.builder()
                .photoUrl(url)
                .orderNo(order)
                .width(width)
                .height(height)
                .build();
    }
}
