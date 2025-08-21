package shinhan.mohaemoyong.server.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import shinhan.mohaemoyong.server.dto.OccurrenceDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_series")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlanSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "series_id")
    private Long seriesId;

    /** 원본 일정(Plans)과 1:1 */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false, unique = true)
    private Plans plan;

    /** 시리즈 활성 여부 */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /** DAILY | WEEKLY | MONTHLY */
    @Column(name = "freq", nullable = false, length = 16)
    private String freq;

    /** 매 n일/주/월 */
    @Column(name = "interval_n", nullable = false)
    private Integer intervalN;

    /** WEEKLY일 때 요일 목록: "MO,TU,WE" */
    @Column(name = "by_days", length = 32)
    private String byDays;

    /** 종료일 (until) */
    @Column(name = "until_date")
    private LocalDate untilDate;

    /** 반복 횟수 (count) */
    @Column(name = "count_n")
    private Integer countN;

    /** 타임존 (확장성 고려) */
    @Column(name = "tz", nullable = false, length = 64)
    private String tz;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ===== 편의 메서드 ===== */
    public void attachToPlan(Plans plan) {
        this.plan = plan;
    }

    public void disable() {
        this.enabled = false;
    }

    public void updateUntil(LocalDate newUntil) {
        this.untilDate = newUntil;
    }

    public List<OccurrenceDto> expandOccurrences(LocalDateTime from, LocalDateTime to) {
        List<OccurrenceDto> occurrences = new ArrayList<>();

        LocalDateTime curStart = plan.getStartTime();
        LocalDateTime curEnd   = plan.getEndTime();

        // 원본 일정의 duration 계산
        var duration = java.time.Duration.between(plan.getStartTime(), plan.getEndTime());

        // untilDate 제한
        LocalDateTime limitEnd = (untilDate != null)
                ? untilDate.atTime(curEnd.toLocalTime()) // untilDate의 끝까지
                : to; // 없으면 요청 구간 to 까지

        while (!curStart.isAfter(to) && !curStart.isAfter(limitEnd)) {
            // 구간에 걸치는 경우만 추가
            if (!(curEnd.isBefore(from) || curStart.isAfter(to))) {
                occurrences.add(new OccurrenceDto(
                        plan.getPlanId(),
                        plan.getTitle(),
                        plan.getPlace(),
                        curStart,
                        curEnd
                ));
            }

            if ("WEEKLY".equalsIgnoreCase(freq)) {
                curStart = curStart.plusWeeks(intervalN != null ? intervalN : 1);
                curEnd   = curStart.plus(duration);
            } else if ("DAILY".equalsIgnoreCase(freq)) {
                curStart = curStart.plusDays(intervalN != null ? intervalN : 1);
                curEnd   = curStart.plus(duration);
            } else {
                break;
            }
        }

        return occurrences;
    }



}
