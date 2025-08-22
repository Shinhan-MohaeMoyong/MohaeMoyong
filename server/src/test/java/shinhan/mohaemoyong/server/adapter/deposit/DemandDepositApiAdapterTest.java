package shinhan.mohaemoyong.server.adapter.deposit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.web.servlet.HandlerMapping;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CreateDemandDepositAccountResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CreateDemandDepositResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.CreateDemandDepositResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireTransactionHistoryListResponse;
import shinhan.mohaemoyong.server.service.financedto.InquireTransactionHistoryListRequestDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//수시입출금 워크플로우
//
//[메인 프로세스]
//은행코드 조회 (교육생/앱 관리자) -> 수시입출금 상품 등록 (교육생/앱 관리자) -> 수시입출금 상품 조회 -> 수시입출금 계좌 생성 -> 계좌 목록 조회
//
//[계좌 목록 조회 후 연계 기능]
//- 계좌 입금 - 계좌 이체 - 이체 한도 변경 - 계좌 거래내역 조회 - 계좌 거래내역 조회 (단건)
//- 계좌 해지 - 계좌 조회(단건) - 예금주 조회 - 계좌 잔액 조회 - 계좌 출금
//
//[상품등록시 참고사항]
//사용자에게 수시입출금 상품 목록을 보여주기 위해 교육생(개발 앱)은 수시입출금 상품 등록 API를 통해 수시입출금 상품을 생성해야합니다.
//    - 은행코드 조회 후 은행별 수시입출금 상품을 등록합니다.
//   - 수시입출금 상품 조회 API를 통해 샘플 데이터를 참고하여 상품을 등록할 수 있습니다.
//
//        [기타 참고사항]
//        - 예금, 적금, 대출 계좌와 카드를 생성하기 위해서는 수시입출금 계좌가 (연결계좌) 존재해야합니다.
//        - 예금, 적금, 대출 계좌와 카드에 연결된 수시입출금 계좌는 (연결계좌) 해지할 수 없습니다.





@Slf4j
@SpringBootTest // Spring Boot 환경을 그대로 불러와 모든 Bean을 사용할 수 있게 해주는 마법 같은 어노테이션
class DemandDepositApiAdapterTest {

    @Autowired // Spring 컨테이너에 등록된 실제 DemandDepositApiAdapter Bean을 주입받음
    private DemandDepositApiAdapter demandDepositApiAdapter;
    @Autowired
    private HandlerMapping resourceHandlerMapping;

    // 앱 개발자가 사용해야하는 매서드
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
    @DisplayName("수시입출금_계좌생성_API_호출_테스트")
    void 수시입출금_계좌생성_API_호출_테스트() {
        // 계좌 생성을 위해 상품번호가 필요함. 상품조회 메서드 호출 후 사용자가 상품을 선택하게 해서 accountTypeUniqueNo 결정
        InquireDemandDepositListResponse response = demandDepositApiAdapter.inquireDemandDepositList();

        // 1. 응답에서 Record 목록을 가져옵니다.
        List<InquireDemandDepositListResponse.Record> records = response.getREC();

        // 2. 목록이 비어있지 않은지 확인하고, 첫 번째 항목을 꺼냅니다.
        // 중요!!! 여기서 비지니스 로직이 필요함. 사용자가 선택하는 상품의 accountTypeUniqueNo를 계좌생성 API로 넘겨줘야함
        if (records != null && !records.isEmpty()) {
            InquireDemandDepositListResponse.Record firstRecord = records.get(0);
            String accountTypeUniqueNo = firstRecord.getAccountTypeUniqueNo();

            log.info("첫 번째 상품의 고유 번호: {}", accountTypeUniqueNo);

            // userkey 하드코딩
            // 중요!! 여기서 repository에 접근하여 이메일에 맞는 userKey를 불러와야함.
            String userKey = "c50e9509-6583-45ba-9ed7-21e53e06be57";
            CreateDemandDepositAccountResponse createDemandDepositResponse = demandDepositApiAdapter.createDemandDepositAccount(userKey, accountTypeUniqueNo);
        }
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