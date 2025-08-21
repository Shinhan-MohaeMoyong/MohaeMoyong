package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.PlanOverride;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanOverrideRepository extends JpaRepository<PlanOverride, Long> {

    /** 특정 시리즈의 날짜 한 건(유니크) 조회 */
    Optional<PlanOverride> findBySeries_SeriesIdAndOccurrenceDate(Long seriesId, LocalDate occurrenceDate);

    /** 특정 시리즈의 날짜 구간 오버라이드 일괄 조회 */
    List<PlanOverride> findBySeries_SeriesIdAndOccurrenceDateBetween(
            Long seriesId, LocalDate from, LocalDate to
    );

    /** 다건 시리즈 + 기간으로 한 번에 긁어오고 싶을 때 (IN 쿼리) */
    @Query("""
        select o
          from PlanOverride o
         where o.series.seriesId in :seriesIds
           and o.occurrenceDate between :from and :to
    """)
    List<PlanOverride> findBySeriesIdsInRange(
            @Param("seriesIds") List<Long> seriesIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
