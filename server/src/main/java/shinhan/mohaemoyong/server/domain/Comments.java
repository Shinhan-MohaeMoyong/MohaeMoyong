package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;

import java.time.Instant;
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

    @Lob
    @Column(name = "content")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Comments(1) ↔ CommentPhotos(N) */
    @OneToMany(mappedBy = "comment",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CommentPhotos> photos = new ArrayList<>();

    // 내부 전용
    void setPlanInternal(Plans plan) { this.plan = plan; }
    void setUserInternal(User user) { this.user = user; }

    void addPhoto(CommentPhotos photo) {
        photos.add(photo);
        if (photo.getComment() != this) photo.setCommentInternal(this);
    }
}
