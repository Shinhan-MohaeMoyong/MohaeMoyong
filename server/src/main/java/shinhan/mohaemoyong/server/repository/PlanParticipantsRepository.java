package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.PlanParticipants;

public interface PlanParticipantsRepository extends JpaRepository<PlanParticipants, Long> {
    boolean existsByPlan_PlanIdAndUser_Id(Long planId, Long userId);

    @Query("""
        select case when count(pp) > 0 then true else false end
        from PlanParticipants pp
        where pp.plan.planId = :planId
          and exists (
              select 1 from Friendship f
              where (
                    (f.user.id = :viewerId and f.friend.id = pp.user.id)
                 or (f.user.id = pp.user.id and f.friend.id = :viewerId)
              )
          )
    """)
    boolean existsFriendWithAnyParticipant(@Param("planId") Long planId,
                                           @Param("viewerId") Long viewerId);
}
