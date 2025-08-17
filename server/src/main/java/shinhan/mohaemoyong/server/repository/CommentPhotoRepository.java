package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.CommentPhotos;

import java.util.Collection;
import java.util.List;

public interface CommentPhotoRepository extends JpaRepository<CommentPhotos, Long> {

    interface PhotoRow {
        Long getCommentId();
        String getPhotoUrl();
        Integer getOrderNo();
    }

    @Query("""
           SELECT p.comment.commentId AS commentId,
                  p.photoUrl          AS photoUrl,
                  p.orderNo           AS orderNo
           FROM CommentPhotos p
           WHERE p.comment.commentId IN :commentIds
           ORDER BY p.comment.commentId ASC, p.orderNo ASC
           """)
    List<PhotoRow> findRowsByCommentIds(@Param("commentIds") Collection<Long> commentIds);
}
