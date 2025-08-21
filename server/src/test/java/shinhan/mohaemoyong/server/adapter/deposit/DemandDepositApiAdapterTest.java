package shinhan.mohaemoyong.server.adapter.deposit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shinhan.mohaemoyong.server.adapter.deposit.dto.CreateDemandDepositResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.InquireTransactionHistoryListResponse;
import shinhan.mohaemoyong.server.service.financedto.InquireTransactionHistoryListRequestDto;

import java.util.List;

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

    @Test
    @DisplayName("수시입출금 계좌 목록 조회 API 호출 테스트") // 테스트에 한글 이름 붙이기
    void 수시입출금_계좌_목록_조회_API_호출_테스트() {
        // given - 테스트에 필요한 값들을 하드코딩으로 준비
        String userKey = "c50e9509-6583-45ba-9ed7-21e53e06be57";

        // when - 실제 어댑터 메서드 호출
        InquireDemandDepositAccountListResponse response =  demandDepositApiAdapter.inquireDemandDepositAccountList(userKey);

        // then - 결과 검증 및 로그 출력
        log.info("응답 코드: {}", response.getHeader().getResponseCode());

        // 계좌목록 출력
            List<InquireDemandDepositAccountListResponse.Record> records = response.getREC();

        if (records != null) {
            records.stream().map(record -> record.getAccountName())
            .forEach(accountname -> log.info("계좌이름 : {}", accountname));
        }


        // 응답 객체가 null이 아닌지 간단히 검증
        assertNotNull(response);
        assertNotNull(response.getHeader());
    }

    @Test
    @DisplayName("수시입출금 계좌거래내역조회 API 호출 테스트") // 테스트에 한글 이름 붙이기
    void 수시입출금_계좌거래내역조회_API_호출_테스트() {
        // given - 테스트에 필요한 값들을 DTO로 준비
        String userKey = "c50e9509-6583-45ba-9ed7-21e53e06be57";

        // 예시 DTO 생성
        InquireTransactionHistoryListRequestDto requestDto = new InquireTransactionHistoryListRequestDto(
                "0017140134561303", // 조회할 계좌번호
                "20250721",         // 조회 시작일
                "20250820",         // 조회 종료일
                "A",                // 거래 구분 (전체)
                "ASC"               // 정렬 순서 (오름차순)
        );
        

        // when - 실제 어댑터 메서드 호출
        InquireTransactionHistoryListResponse response =  demandDepositApiAdapter.inquireTransactionHistoryList(userKey, requestDto);

            
        // then - 결과 검증 및 로그 출력
        log.info("응답 코드: {}", response.getHeader().getResponseCode());

        // 계좌목록 출력
        List<InquireTransactionHistoryListResponse.Transaction> records = response.getREC().getList();

        log.info("총 거래 건수 : {}", response.getREC().getTotalCount());

        if (records != null) {
            records.stream().map(record -> record.getTransactionUniqueNo())
                    .forEach(transactionuniqueNo -> log.info("거래고유번호 : {}", transactionuniqueNo));
        }

        // 응답 객체가 null이 아닌지 간단히 검증
        assertNotNull(response);
        assertNotNull(response.getHeader());
    }
}