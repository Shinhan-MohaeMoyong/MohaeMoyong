package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.dto.DetailPlanUpdateRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
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

    @Column(name = "image_url", length = 512) // 255 초과
    private String imageUrl;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "place", length = 255)
    private String place;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "has_savings_goal", nullable = false)
    private boolean hasSavingsGoal = false;

    @Column(name = "savings_amount")
    private Integer savingsAmount;

    // enum 대신 문자열
    @Column(name = "privacy_level", nullable = false, length = 255)
    private String privacyLevel = "PUBLIC";

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
    @OneToMany(mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PlanPhotos> photos = new ArrayList<>();

    /** Plans(1) ↔ PlanParticipants(N) */
    @OneToMany(mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PlanParticipants> participants = new ArrayList<>();

    /** Plans(1) ↔ Comments(N) */
    @OneToMany(mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    // 내부 전용 (Users와의 양방향 연결 보조)
    void setUserInternal(User user) { this.user = user; }

    // 내부 전용 편의 메서드
    void addPhoto(PlanPhotos photo) {
        photos.add(photo);
        if (photo.getPlan() != this) photo.setPlanInternal(this);
    }
    void addParticipant(PlanParticipants pp) {
        participants.add(pp);
        if (pp.getPlan() != this) pp.setPlanInternal(this);
    }
    void addComment(Comments c) {
        comments.add(c);
        if (c.getPlan() != this) c.setPlanInternal(this);
    }


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

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void softDelete(LocalDateTime now) {
        if (this.deletedAt == null) {
            this.deletedAt = now;
            this.updatedAt = now;
        }
    }
}
