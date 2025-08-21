package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.PlanSeries;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanSeriesRepository extends JpaRepository<PlanSeries, Long> {

    /** planId로 1:1 시리즈 조회 */
    Optional<PlanSeries> findByPlan_PlanId(Long planId);

    /** 특정 유저의 활성 시리즈 조회 */
    List<PlanSeries> findByPlan_User_IdAndEnabledTrue(Long userId);

    /** 주어진 기간과 교차하는(가능성 있는) 시리즈 대략 조회용 — 필요 시 튜닝 */
    @Query("""
        select s
          from PlanSeries s
         where s.enabled = true
           and s.plan.user.id = :userId
           and (
                 s.untilDate is null
                 or s.untilDate >= :from
               )
    """)
    List<PlanSeries> findActiveSeriesRoughlyInRange(
            @Param("userId") Long userId,
            @Param("from") LocalDate from
    );
}
