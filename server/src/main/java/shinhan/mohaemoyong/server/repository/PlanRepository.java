package shinhan.mohaemoyong.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.FriendPlanDto;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plans, Long> {

    // 내 이번주 일정 조회
    @Query("""
        SELECT p.planId AS planId,
               p.title AS title,
               p.place AS place,
               p.startTime AS startTime,
               p.endTime AS endTime
        FROM Plans p
        WHERE p.user.id = :userId
          AND p.deletedAt IS NULL
          AND p.startTime < :endOfWeek
          AND p.endTime   >= :startOfWeek
          AND p.privacyLevel <> 'PRIVATE'
        ORDER BY p.startTime ASC
    """)
    List<HomeWeekResponse> findWeeklyPlans(
            @Param("userId") Long userId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );

    // 내 전체 일정 조회
    @Query("""
    SELECT p.planId AS planId,
           p.title AS title,
           p.place AS place,
           p.startTime AS startTime,
           p.endTime AS endTime
    FROM Plans p
    WHERE p.user.id = :userId
      AND p.deletedAt IS NULL
    ORDER BY p.startTime ASC
""")
    List<HomeWeekResponse> findAllPlansByUserId(@Param("userId") Long userId);


    // 친구 일정 (오늘 ~ +7일 범위, rolling window)
    @Query("""
        SELECT p FROM Plans p
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

    // 친구 전체 공개 일정
    @Query("""
        SELECT new shinhan.mohaemoyong.server.dto.FriendPlanDto(
            p.planId, p.title, p.place, p.startTime, p.endTime
        )
        FROM Plans p
        WHERE p.user.id = :friendId
          AND p.deletedAt IS NULL
          AND p.privacyLevel = 'PUBLIC'
        ORDER BY p.startTime ASC
    """)
    List<FriendPlanDto> findAllPublicPlansOfUser(@Param("friendId") Long friendId);



    /** DetailPlan 조회(작성자 fetch). 댓글은 분리 API에서 처리 */
    @Query("""
        select p
        from Plans p
          join fetch p.user u
        where p.planId = :planId
          and u.id = :userId
          and p.deletedAt is null
    """)
    Optional<Plans> findDetailByOwner(@Param("userId") Long userId,
                                      @Param("planId") Long planId);


    /*댓글수 동기화*/
    @Modifying
    @Query("""
        update Plans p
           set p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END
         where p.planId = :planId
    """)
    int decrementCommentCountSafely(Long planId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Plans p set p.commentCount = coalesce(p.commentCount,0) + 1 where p.planId = :planId")
    int incrementCommentCount(@Param("planId") Long planId);

}
