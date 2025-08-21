package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.dto.DetailPlanUpdateRequest;
import shinhan.mohaemoyong.server.dto.OccurrenceDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Plans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    // 스키마상 컬럼명이 'id' (users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 대표 이미지(썸네일)
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "place", length = 255)
    private String place;

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Builder.Default
    @Column(name = "has_savings_goal", nullable = false)
    private boolean hasSavingsGoal = false;

    @Column(name = "savings_amount")
    private Integer savingsAmount;

    // enum 대신 문자열
    @Column(name = "privacy_level", nullable = false, length = 255)
    private String privacyLevel = "PUBLIC";

    @Builder.Default
    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Plans(1) ↔ PlanPhotos(N) */
    @Builder.Default
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanPhotos> photos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanParticipants> participants = new ArrayList<>();

    /** Plans(1) ↔ Comments(N) */
    @Builder.Default
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    // privacyLevel은 서비스에서 확실히 세팅하니 기본값을 두지 않거나, 둔다면 Builder.Default로
    // @Builder.Default
    // private String privacyLevel = "PERSONAL_PUBLIC";

    // ===================== 연관관계/도메인 메서드 =====================

    // Users와의 양방향 연결 보조
    void setUserInternal(User user) { this.user = user; }

    // 사진 추가 (PlanPhotos 생성과 연관관계 세팅을 한 곳에서)
    public void addPhoto(String url, Integer order, Integer width, Integer height) {
        PlanPhotos photo = PlanPhotos.of(url, order, width, height);
        photo.setPlanInternal(this); // 같은 패키지에서만 접근
        this.photos.add(photo);
    }

    // 대표 이미지 지정
    public void changeThumbnailTo(String url) { this.imageUrl = url; }

    // 참가자 추가 (중복 방지 + 역방향 세팅)
    public void addParticipant(PlanParticipants pp) {
        if (!participants.contains(pp)) {
            participants.add(pp);
            if (pp.getPlan() != this) pp.setPlanInternal(this);
        }
    }

    void addComment(Comments c) {
        comments.add(c);
        if (c.getPlan() != this) c.setPlanInternal(this);
    }

    // ===================== 비즈니스 로직 =====================

    public void applyUpdate(DetailPlanUpdateRequest req, LocalDateTime now) {
        if (req.title()        != null) this.title = req.title();
        if (req.content()      != null) this.content = req.content();
        if (req.imageUrl()     != null) this.imageUrl = req.imageUrl();
        if (req.place()        != null) this.place = req.place();
        if (req.startTime()    != null) this.startTime = req.startTime();
        if (req.endTime()      != null) this.endTime = req.endTime();
        if (req.isCompleted()  != null) this.isCompleted = req.isCompleted();
        if (req.hasSavingsGoal()!= null) this.hasSavingsGoal = req.hasSavingsGoal();

        // savingsAmount 규칙
        if (req.hasSavingsGoal() != null) {
            if (req.hasSavingsGoal()) {
                if (req.savingsAmount() != null) this.savingsAmount = req.savingsAmount();
            } else {
                this.savingsAmount = null;
            }
        } else if (req.savingsAmount() != null && this.hasSavingsGoal) {
            this.savingsAmount = req.savingsAmount();
        }

        if (req.privacyLevel() != null) this.privacyLevel = req.privacyLevel();
        this.updatedAt = now;
    }

    public void increaseCommentCountInMemory() {
        commentCount = (commentCount == null ? 1 : commentCount + 1);
    }

    public boolean isDeleted() { return this.deletedAt != null; }

    public void softDelete(LocalDateTime now) {
        if (this.deletedAt == null) {
            this.deletedAt = now;
            this.updatedAt = now;
        }
    }

    @OneToOne(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlanSeries planSeries;

    public PlanSeries getPlanSeries() {
        return planSeries;
    }

    public void setPlanSeries(PlanSeries planSeries) {
        this.planSeries = planSeries;
        if (planSeries != null && planSeries.getPlan() != this) {
            planSeries.attachToPlan(this);
        }
    }



}
