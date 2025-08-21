package shinhan.mohaemoyong.server.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OccurrencePatchRequest(
        LocalDate occurrenceDate,
        OccurrenceScope scope,
        String title,
        String content,
        String place,
        LocalDateTime startTime,
        LocalDateTime endTime,
        PrivacyLevel privacyLevel,
        String imageUrl
) {}
