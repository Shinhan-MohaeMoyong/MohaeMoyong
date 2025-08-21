package shinhan.mohaemoyong.server.adapter.deposit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CreateDemandDepositResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest // Spring Boot 환경을 그대로 불러와 모든 Bean을 사용할 수 있게 해주는 마법 같은 어노테이션
class DemandDepositApiAdapterTest {

    @Autowired // Spring 컨테이너에 등록된 실제 DemandDepositApiAdapter Bean을 주입받음
    private DemandDepositApiAdapter demandDepositApiAdapter;

    @Test
    @DisplayName("수시입출금 상품 등록 API 호출 테스트") // 테스트에 한글 이름 붙이기
    void 수시입출금_상품_등록_API_호출_테스트() {
        // given - 테스트에 필요한 값들을 하드코딩으로 준비
        String bankCode = "088";
        String accountName = "모해모여 테스트 상품";
        String accountDescription = "통합 테스트용 상품입니다.";

        // when - 실제 어댑터 메서드 호출
        CreateDemandDepositResponse response = demandDepositApiAdapter.createDemandDeposit(bankCode, accountName, accountDescription);

        // then - 결과 검증 및 로그 출력
        log.info("응답 코드: {}", response.getHeader().getResponseCode());
        log.info("등록된 상품명: {}", response.getREC().getAccountName());

        // 응답 객체가 null이 아닌지 간단히 검증
        assertNotNull(response);
        assertNotNull(response.getHeader());
    }
}