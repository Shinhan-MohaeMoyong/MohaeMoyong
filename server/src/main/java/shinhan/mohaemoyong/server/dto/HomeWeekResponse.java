package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;

public interface HomeWeekResponse {
    Long getPlanId();
    String getTitle();
    String getPlace();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
