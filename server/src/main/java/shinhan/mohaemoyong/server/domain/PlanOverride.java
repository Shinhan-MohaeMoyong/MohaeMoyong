package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "plan_overrides",
        uniqueConstraints = @UniqueConstraint(name = "uk_series_occurrence", columnNames = {"series_id", "occurrence_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class PlanOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "override_id")
    private Long overrideId;

    /** 시리즈와 N:1 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private PlanSeries series;

    /** 발생 기준일(원본 시작시각의 날짜) */
    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    /** 해당 발생건을 삭제(취소) 처리할지 여부 */
    @Column(name = "is_cancelled", nullable = false)
    private boolean cancelled;

    /* ====== 오버라이드(수정) 필드들: null이면 원본 값 유지 ====== */
    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "place", length = 255)
    private String place;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "privacy_level", length = 64)
    private String privacyLevel;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ===== 편의 메서드 ===== */
    public void attachToSeries(PlanSeries series) {
        this.series = series;
    }

    public void cancelOnly() {
        this.cancelled = true;
        // 다른 오버라이드 값들은 그대로 두어도 됨(보통 null)
    }

    public void applyPatchFrom(PlanOverride patch) {
        if (patch.title != null) this.title = patch.title;
        if (patch.content != null) this.content = patch.content;
        if (patch.place != null) this.place = patch.place;
        if (patch.startTime != null) this.startTime = patch.startTime;
        if (patch.endTime != null) this.endTime = patch.endTime;
        if (patch.privacyLevel != null) this.privacyLevel = patch.privacyLevel;
        if (patch.imageUrl != null) this.imageUrl = patch.imageUrl;
        // cancelled는 별도 처리
    }
}
