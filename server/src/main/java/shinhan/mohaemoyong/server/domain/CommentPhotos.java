package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.domain.Comments;

import java.time.Instant;

@Entity
@Table(name = "comment_photos")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentPhotos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_photo_id")
    private Long commentPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comments comment;

    @Column(name = "photo_url", nullable = false, length = 512) // 255 초과
    private String photoUrl;

    @Column(name = "order_no")
    private Integer orderNo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 내부 전용
    void setCommentInternal(Comments c) { this.comment = c; }

    public static CommentPhotos create(String url, Integer orderNo) {
        CommentPhotos p = new CommentPhotos();
        p.photoUrl = url;
        p.orderNo  = (orderNo == null ? 0 : orderNo);
        return p;
    }
}
