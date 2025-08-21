package shinhan.mohaemoyong.server.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Builder
public class SearchAccountResponseDto {
    private String accountNumber;      // 계좌번호
    private Long balance;              // 잔여금액
    private String accountAlias;       // 계좌별칭
    private List<WeeklySavingDto> monthlySavings; // 월별 저축 금액
    private Double achievementRate;    // 계좌 저축금액 달성률
}


