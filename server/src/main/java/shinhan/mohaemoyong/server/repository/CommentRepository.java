package shinhan.mohaemoyong.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.Comments;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comments, Long> {
    /** 댓글 수 찾기 */
    @Query("""
           SELECT p.commentCount
           FROM Plans p
           WHERE p.planId = :planId
             AND p.deletedAt IS NULL
           """)
    Optional<Integer> findCommentCountByPlanId(@Param("planId") Long planId);

    /** 1단계: 댓글 ID만 페이징 (소프트삭제 제외, 최신순) */
    @Query("""
           SELECT c.commentId
           FROM Comments c
           WHERE c.plan.planId = :planId
             AND c.deletedAt IS NULL
           ORDER BY c.createdAt DESC
           """)
    Page<Long> findPageIdsByPlanId(@Param("planId") Long planId, Pageable pageable);

    /** 2단계: ID 묶음으로 댓글 + 작성자(User)만 fetch join (컬렉션 제외) */
    @Query("""
           SELECT c
           FROM Comments c
           JOIN FETCH c.user u
           WHERE c.commentId IN :ids
           """)
    List<Comments> findWithUserByIdIn(@Param("ids") Collection<Long> ids);

    /** planId로 Comment 들고오기 **/
    @Query("""
        select c
        from Comments c
        join fetch c.plan p
        join fetch c.user u
        where c.commentId = :commentId
          and p.planId   = :planId
          and c.deletedAt is null
    """)
    Optional<Comments> findActiveByIdAndPlanId(Long commentId, Long planId);


    Optional<Comments> findByCommentIdAndPlan_PlanIdAndDeletedAtIsNull(Long commentId, Long planId);

}