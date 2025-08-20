package shinhan.mohaemoyong.server.adapter.deposit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;
import shinhan.mohaemoyong.server.adapter.common.factory.HeaderFactory;
import shinhan.mohaemoyong.server.adapter.deposit.dto.*;
//import shinhan.mohaemoyong.server.adapter.deposit.dto.UpdateDemandDepositAccountTransferRequest;
//import shinhan.mohaemoyong.server.adapter.deposit.dto.UpdateDemandDepositAccountTransferResponse;
//import shinhan.mohaemoyong.server.adapter.deposit.dto.CreateDemandDepositRequest;
//import shinhan.mohaemoyong.server.adapter.deposit.dto.CreateDemandDepositResponse;


@Slf4j
@Component
public class DemandDepositApiAdapter {
    private final RestTemplate restTemplate;
    private final HeaderFactory headerFactory;

    // application.properties 파일에서 설정 정보 가져오기
    @Value("${api.shinhan.base-url}")
    private String baseUrl;

    @Value("${api.shinhan.api-key}")
    private String apiKey;

    // 생성자를 통해 RestTemplate Bean을 주입받습니다.
    public DemandDepositApiAdapter(RestTemplate restTemplate, HeaderFactory headerFactory) {
        this.restTemplate = restTemplate;
        this.headerFactory = headerFactory;
    }

    /**
     * 수시입출금 상품을 등록하는 API를 호출합니다.
     *
     * @param bankCode             은행 코드
     * @param accountName          상품명
     * @param accountDescription   상품 설명
     * @return 등록된 상품 정보가 담긴 DTO
     */
    public CreateDemandDepositResponse createDemandDeposit(String bankCode, String accountName, String accountDescription) {
        // 1. API 요청을 위한 URL 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/createDemandDeposit";
        
        // HeaderFactory를 사용하여 공통 헤더를 생성합니다. (userKey 제외)
        RequestHeader header = headerFactory.createHeader("createDemandDeposit");

        // 요청 DTO를 생성합니다.
        CreateDemandDepositRequest requestBody = new CreateDemandDepositRequest(header, bankCode, accountName, accountDescription);

        log.info("수시입출금 상품 등록 요청: {}", accountName);

        try {
            // 2. RestTemplate을 사용하여 POST 요청을 보내고 응답을 받습니다.
            CreateDemandDepositResponse response = restTemplate.postForObject(url, requestBody, CreateDemandDepositResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("수시입출금 상품 등록 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;

        } catch (Exception e) {
            // 3. API가 4xx 에러(H1000 등)를 반환했을 때 예외를 처리합니다.
            log.error("수시입출금 상품 등록 실패. 에러: {}", e.getMessage());
            log.error("수시입출금 상품 등록 중 API 응답 처리 실패.", e);
            throw new RuntimeException("상품 등록에 실패했습니다.");
        }
    }


    /**
     * 수시입출금 은행별 상품 목록을 조회하는 API를 호출합니다.
     *
     * @return 조회된 상품 목록 정보가 담긴 DTO
     */
    public InquireDemandDepositListResponse inquireDemandDepositList() {
        // 1. API 요청을 위한 URL과 Body를 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositList";

        // 이 API는 userKey를 제외하므로, 파라미터가 1개인 createHeader 메서드를 사용합니다.
        RequestHeader header = headerFactory.createHeader("inquireDemandDepositList");
        InquireDemandDepositListRequest requestBody = new InquireDemandDepositListRequest(header);

        log.info("은행별 상품 목록 조회 요청");

        try {
            // 2. RestTemplate을 사용하여 POST 요청을 보내고 응답을 받습니다.
            InquireDemandDepositListResponse response = restTemplate.postForObject(url, requestBody, InquireDemandDepositListResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("상품 목록 조회 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;

        } catch (Exception e) {
            // 3. 에러 발생 시 예외를 처리합니다.
            log.error("상품 목록 조회 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("상품 목록 조회에 실패했습니다.");
        }
    }


    /**
     * 계좌 생성 API를 호출합니다.
     *
     * @param userKey             사용자 고유 키
     * @param accountTypeUniqueNo 상품 고유 번호
     * @return 생성된 계좌 정보가 담긴 DTO
     */
    public CreateDemandDepositAccountResponse createDemandDepositAccount(String userKey, String accountTypeUniqueNo) {
        // 1. API 요청을 위한 URL과 Body를 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/createDemandDepositAccount";

        // 이 API는 userKey를 포함하므로, 파라미터가 2개인 createHeader 메서드를 사용합니다.
        RequestHeader header = headerFactory.createHeader("createDemandDepositAccount", userKey);
        CreateDemandDepositAccountRequest requestBody = new CreateDemandDepositAccountRequest(header, accountTypeUniqueNo);

        log.info("계좌 생성 요청: 상품 고유 번호 [{}]", accountTypeUniqueNo);

        try {
            // 2. RestTemplate을 사용하여 POST 요청을 보내고 응답을 받습니다.
            CreateDemandDepositAccountResponse response = restTemplate.postForObject(url, requestBody, CreateDemandDepositAccountResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("계좌 생성 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;

        } catch (Exception e) {
            // 3. 에러 발생 시 예외를 처리합니다.
            log.error("계좌 생성 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("계좌 생성에 실패했습니다.");
        }
    }

    // 계좌이체 금융 API
    /**
     * 계좌 이체 API를 호출합니다. (새로 추가된 메서드)
     *
     * @param userKey                   사용자 고유 키
     * @param withdrawalAccountNo       출금 계좌 번호
     * @param depositAccountNo          입금 계좌 번호
     * @param transactionBalance        거래 금액
     * @return 이체 결과가 담긴 DTO
     */

    public UpdateDemandDepositAccountTransferResponse transfer(String userKey, String withdrawalAccountNo, String depositAccountNo, Long transactionBalance) {
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/updateDemandDepositAccountTransfer";
        RequestHeader header = headerFactory.createHeader("updateDemandDepositAccountTransfer", userKey);

        UpdateDemandDepositAccountTransferRequest requestBody = new UpdateDemandDepositAccountTransferRequest(
                header,
                depositAccountNo,
                transactionBalance,
                withdrawalAccountNo,
                "입금(이체)",
                "출금(이체)"
        );

        log.info("계좌 이체 요청: 출금 [{}], 입금 [{}], 금액 [{}]", withdrawalAccountNo, depositAccountNo, transactionBalance);

        try {
            UpdateDemandDepositAccountTransferResponse response = restTemplate.postForObject(url, requestBody, UpdateDemandDepositAccountTransferResponse.class);
            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }
            log.info("계좌 이체 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;
        } catch (Exception e) {
            log.error("계좌 이체 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("계좌 이체에 실패했습니다.");
        }
    }


}
