package shinhan.mohaemoyong.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HomeWeekResponse(
        Long planId,
        String seriesId,
        Integer occurrenceIndex,
        String title,
        String content,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String imageUrl,          // 대표 사진
        List<String> photos       // 추가 사진
) {}
