package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendPlanDto {

    private Long planId;
    private String title;
    private String place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
