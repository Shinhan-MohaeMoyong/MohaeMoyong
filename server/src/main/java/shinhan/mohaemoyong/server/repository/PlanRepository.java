package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.FriendPlanDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plans, Long> {

    /** ✅ 내 이번주 일정 조회 (내가 만든 일정 + 그룹 초대 일정, photos 포함) */
    @Query("""
        SELECT DISTINCT p
        FROM Plans p
        LEFT JOIN FETCH p.photos
        LEFT JOIN p.participants pp
        WHERE (p.user.id = :userId OR pp.user.id = :userId)
          AND p.deletedAt IS NULL
          AND p.startTime < :endOfWeek
          AND p.endTime   >= :startOfWeek
        ORDER BY p.startTime ASC
    """)
    List<Plans> findPlansByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );

    /** ✅ 내 전체 일정 조회 (내가 만든 일정 + 그룹 초대 일정, photos 포함) */
    @Query("""
        SELECT DISTINCT p
        FROM Plans p
        LEFT JOIN FETCH p.photos
        LEFT JOIN p.participants pp
        WHERE (p.user.id = :userId OR pp.user.id = :userId)
          AND p.deletedAt IS NULL
        ORDER BY p.startTime ASC
    """)
    List<Plans> findPlansByUserIdWithPhotos(@Param("userId") Long userId);

    /** ✅ 친구 일정 (오늘 ~ +7일 rolling window, 공개된 개인 일정만) */
    @Query("""
        SELECT p
        FROM Plans p
        WHERE p.user.id = :friendId
          AND p.deletedAt IS NULL
          AND p.privacyLevel = 'PUBLIC'
          AND p.startTime < :end
          AND p.endTime   >= :start
        ORDER BY p.startTime ASC
    """)
    List<Plans> findRecentPublicPlansWithinRange(
            @Param("friendId") Long friendId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /** ✅ 친구 이번주 공개 일정 (개인 + 그룹 초대) */
    @Query("""
    SELECT DISTINCT p
    FROM Plans p
    LEFT JOIN p.participants part
    WHERE p.deletedAt IS NULL
      AND (p.user.id = :friendId OR part.user.id = :friendId)
      AND p.privacyLevel = 'PUBLIC'
      AND p.startTime < :endOfWeek
      AND p.endTime   >= :startOfWeek
    ORDER BY p.startTime ASC
""")
    List<Plans> findFriendWeeklyPublicPlans(
            @Param("friendId") Long friendId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );



    /** 친구 전체 공개 일정 (개인 + 그룹) */
    @Query("""
    SELECT DISTINCT p
    FROM Plans p
    LEFT JOIN p.participants part
    WHERE p.deletedAt IS NULL
      AND (
        p.user.id = :friendId
        OR part.user.id = :friendId
      )
      AND (p.privacyLevel = 'PERSONAL_PUBLIC' OR p.privacyLevel = 'GROUP_PUBLIC')
    ORDER BY p.startTime ASC
""")
    List<Plans> findFriendAllPublicPlans(@Param("friendId") Long friendId);


    /** 📌 DetailPlan 조회 (작성자 fetch). 댓글은 분리 API에서 처리 */
    @Query("""
        SELECT p
        FROM Plans p
          JOIN FETCH p.user u
        WHERE p.planId = :planId
          AND u.id = :userId
          AND p.deletedAt IS NULL
    """)
    Optional<Plans> findDetailByOwner(
            @Param("userId") Long userId,
            @Param("planId") Long planId
    );

    /** 📌 댓글수 동기화 (감소) */
    @Modifying
    @Query("""
        UPDATE Plans p
           SET p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END
         WHERE p.planId = :planId
    """)
    int decrementCommentCountSafely(Long planId);

    /** 📌 댓글수 동기화 (증가) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Plans p
           SET p.commentCount = coalesce(p.commentCount, 0) + 1
         WHERE p.planId = :planId
    """)
    int incrementCommentCount(@Param("planId") Long planId);

    Long user(User user);

    /** 📌 하루 단위 일정 조회 */
    @Query("""
    SELECT p
    FROM Plans p
      JOIN FETCH p.user u
    WHERE p.startTime >= :startOfDay
      AND p.startTime < :endOfDay
      AND p.deletedAt IS NULL
      AND u.id = :userId
""")
    List<Plans> findPlansByStartDateWithUser(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("userId") Long userId
    );

    @Query("""
    select p
    from Plans p
    join fetch p.user u
    where p.planId = :planId
      and p.deletedAt is null
""")
    Optional<Plans> findDetailById(@Param("planId") Long planId);
}
