package shinhan.mohaemoyong.server.dto;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record RecurrenceCreateReq(
        Boolean enabled,               // true일 경우 반복 설정
        String freq,                   // DAILY | WEEKLY | MONTHLY
        @Positive Integer interval,    // 매 n일/주/월
        List<String> byDays,           // WEEKLY일 때 요일 ["MO","TU","WE",...]
        LocalDate until,               // 종료일 (until 또는 count 중 하나 필수)
        Integer count,                 // 반복 횟수
        List<LocalDate> exceptions     // 제외할 발생일
) {}
