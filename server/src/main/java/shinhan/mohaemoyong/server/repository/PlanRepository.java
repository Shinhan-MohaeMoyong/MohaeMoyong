package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.FriendPlanDto;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plans, Long> {

    // 내 이번주 일정 조회
    @Query("""
        select p
        from Plans p
        left join fetch p.planSeries s
        where p.user.id = :userId
          and p.deletedAt is null
          and (p.startTime < :endOfWeek and p.endTime >= :startOfWeek)
    """)
    List<Plans> findPlansWithinRange(@Param("userId") Long userId,
                                     @Param("startOfWeek") LocalDateTime startOfWeek,
                                     @Param("endOfWeek") LocalDateTime endOfWeek);

    // 내 전체 일정 조회 (엔티티 그대로)
    @Query("""
        select p
        from Plans p
        left join fetch p.planSeries s
        where p.user.id = :userId
          and p.deletedAt is null
        order by p.startTime asc
    """)
    List<Plans> findAllPlansEntityByUserId(@Param("userId") Long userId);

    // 내 전체 일정 조회 (DTO projection)
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

    // 친구 일정 (오늘 ~ +7일 rolling window) — Owner 또는 Participant 포함 + privacy LIKE
    @Query("""
        select distinct p
        from Plans p
        where p.deletedAt is null
          and (
                p.user.id = :friendId
             or exists (
                    select 1
                    from PlanParticipants pp
                    where pp.plan = p
                      and pp.user.id = :friendId
                )
          )
          and p.privacyLevel like '%_PUBLIC'
          and p.startTime < :end
          and p.endTime   >= :start
        order by p.startTime asc
    """)
    List<Plans> findRecentPublicPlansWithinRange(
            @Param("friendId") Long friendId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 이번주 친구 일정 조회
    @Query("""
        select distinct p
        from Plans p
        left join fetch p.planSeries s
        where p.deletedAt is null
          and (
                p.user.id = :friendId
             or exists (
                    select 1
                    from PlanParticipants pp
                    where pp.plan = p
                      and pp.user.id = :friendId
                )
          )
          and p.privacyLevel like '%_PUBLIC'
          and (p.startTime < :endOfWeek and p.endTime >= :startOfWeek)
        order by p.startTime asc
    """)
    List<Plans> findFriendPlansWithinRange(
            @Param("friendId") Long friendId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );

    // 친구 전체 공개 일정
    @Query("""
        select new shinhan.mohaemoyong.server.dto.FriendPlanDto(
            p.planId, p.title, p.place, p.startTime, p.endTime
        )
        from Plans p
        where p.deletedAt is null
          and (
                p.user.id = :friendId
             or exists (
                    select 1
                    from PlanParticipants pp
                    where pp.plan = p
                      and pp.user.id = :friendId
                )
          )
          and p.privacyLevel like '%_PUBLIC'
        order by p.startTime asc
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

    /* 댓글수 동기화 */
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
