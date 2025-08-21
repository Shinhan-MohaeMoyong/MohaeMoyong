package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;

// 조회 응답에 쓰는 DTO (new로 생성 가능)
public class HomeWeekResponse {
    private final Long planId;
    private final String title;
    private final String place;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public HomeWeekResponse(Long planId, String title, String place,
                            LocalDateTime startTime, LocalDateTime endTime) {
        this.planId = planId;
        this.title = title;
        this.place = place;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // OccurrenceDto -> HomeWeekResponse 변환 헬퍼
    public static HomeWeekResponse fromOccurrence(OccurrenceDto occ) {
        return new HomeWeekResponse(
                occ.planId(),
                occ.title(),
                occ.place(),
                occ.startTime(),
                occ.endTime()
        );
    }

    // 게터
    public Long getPlanId() { return planId; }
    public String getTitle() { return title; }
    public String getPlace() { return place; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
