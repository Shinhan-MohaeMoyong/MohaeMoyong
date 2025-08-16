package shinhan.mohaemoyong.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinhan.mohaemoyong.server.domain.Plans;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plans, Long> {

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
        ORDER BY p.startTime ASC
""")
    List<HomeWeekResponse> findWeeklyPlans(
            @Param("userId") Long userId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );

}
