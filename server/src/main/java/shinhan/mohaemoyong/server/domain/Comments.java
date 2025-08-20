package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.CreateCommentRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plans plan;

    // users.id 컬럼명이 'id'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Comments(1) ↔ CommentPhotos(N) */
    @OneToMany(mappedBy = "comment",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CommentPhotos> photos = new ArrayList<>();

    // 내부 전용
    void setPlanInternal(Plans plan) { this.plan = plan; }
    void setUserInternal(User user) { this.user = user; }

    @Builder
    private Comments(Plans plan, User user, String content) {
        this.plan = plan;
        this.user = user;
        this.content = content;
    }
    public static Comments create(Plans plan, User userId, String content) {
        return Comments.builder()
                .plan(plan)
                .user(userId)
                .content(content)
                .build();
    }

    // 단건 추가
    public Comments addPhoto(String url, Integer orderNo) {
        CommentPhotos p = CommentPhotos.create(url, orderNo);
        p.setCommentInternal(this);
        this.photos.add(p);
        return this;
    }

    // 다건 추가
    public Comments addPhotos(List<CreateCommentRequest.PhotoItem> items) {
        if (items == null || items.isEmpty()) return this;
        for (CreateCommentRequest.PhotoItem it : items) {
            addPhoto(it.url(), it.orderNo());
        }
        return this;
    }

    // 다건 url만 받아올 때
    public Comments addPhotosUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return this;
        int idx = 0;
        for (String url : urls) addPhoto(url, idx++);
        return this;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
