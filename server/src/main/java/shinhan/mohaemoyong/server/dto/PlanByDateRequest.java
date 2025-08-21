package shinhan.mohaemoyong.server.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 특정 날짜의 일정을 조회하기 위한 요청 DTO
 */
public record PlanByDateRequest(
        @NotNull // 날짜는 반드시 포함되어야 함
        LocalDate date
) {}