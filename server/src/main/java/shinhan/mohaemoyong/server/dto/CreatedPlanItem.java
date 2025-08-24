// shinhan.mohaemoyong.server.dto.CreatedPlanItem.java
package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;

public record CreatedPlanItem(
        Long planId,
        Integer occurrenceIndex,      // 단일 생성이면 null
        String title,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
