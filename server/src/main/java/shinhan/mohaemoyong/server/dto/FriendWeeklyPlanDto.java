package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class FriendWeeklyPlanDto {
    Long planId;
    String title;
    String place;
    LocalDateTime startTime;
    LocalDateTime endTime;
    boolean isNew;   // lastSeenAt 이후 생성되었는지
}
