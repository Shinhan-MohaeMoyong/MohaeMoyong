package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;

// 반복 일정 전개 결과를 담는 DTO
public record OccurrenceDto(
        Long planId,
        String title,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
