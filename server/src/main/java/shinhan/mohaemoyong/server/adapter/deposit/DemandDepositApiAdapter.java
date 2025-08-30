package shinhan.mohaemoyong.server.adapter.deposit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import shinhan.mohaemoyong.server.adapter.common.factory.HeaderFactory;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;
import shinhan.mohaemoyong.server.adapter.deposit.dto.request.*;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.*;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
import shinhan.mohaemoyong.server.adapter.exception.ExceptionResponseDto;
import shinhan.mohaemoyong.server.service.financedto.InquireTransactionHistoryListRequestDto;


@Slf4j
@Component
public class DemandDepositApiAdapter {
    private final RestTemplate restTemplate;
    private final HeaderFactory headerFactory;
    private final ObjectMapper objectMapper;

    // application.properties 파일에서 설정 정보 가져오기
    @Value("${api.shinhan.base-url}")
    private String baseUrl;

    @Value("${api.shinhan.api-key}")
    private String apiKey;

    @Value("${api.shinhan.authCode-authText}")
    private String authPath;

    // 생성자를 통해 RestTemplate Bean을 주입받습니다.
    public DemandDepositApiAdapter(RestTemplate restTemplate, HeaderFactory headerFactory, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.headerFactory = headerFactory;
        this.objectMapper = objectMapper;
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

    public UpdateDemandDepositAccountTransferResponse transfer(String userKey, String withdrawalAccountNo, String depositAccountNo, Long transactionBalance, String summary) {
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/updateDemandDepositAccountTransfer";
        RequestHeader header = headerFactory.createHeader("updateDemandDepositAccountTransfer", userKey);

        UpdateDemandDepositAccountTransferRequest requestBody = new UpdateDemandDepositAccountTransferRequest(
                header,
                depositAccountNo,
                transactionBalance,
                withdrawalAccountNo,
                summary,
                summary
        );

        log.info("계좌 이체 요청: 출금 [{}], 입금 [{}], 금액 [{}]", withdrawalAccountNo, depositAccountNo, transactionBalance);

        try {
            UpdateDemandDepositAccountTransferResponse response = restTemplate.postForObject(url, requestBody, UpdateDemandDepositAccountTransferResponse.class);
            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }
            log.info("계좌 이체 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;
        } catch (HttpClientErrorException e) { // 4xx 에러 처리
            String errorBody = e.getResponseBodyAsString();
            log.warn("API 클라이언트 오류: {}, 응답: {}", e.getStatusCode(), errorBody);

            ExceptionResponseDto errorResponse;
            try {
                // 1. 이 try-catch 블록은 오직 '파싱'만 책임집니다.
                errorResponse = objectMapper.readValue(errorBody, ExceptionResponseDto.class);
            } catch (Exception parseException) {
                // 2. 파싱 자체를 실패한 경우에만 이곳으로 옵니다.
                log.error("API 에러 응답 파싱 실패", parseException);
                throw new RuntimeException("API 에러 응답을 파싱할 수 없습니다: " + errorBody);
            }

            // 3. 파싱이 성공한 후, 그 결과를 가지고 분기하여 예외를 던집니다.
            if ("A1014".equals(errorResponse.getResponseCode())) {
                log.error("잔액부족(A1014) 에러를 ApiErrorException으로 변환합니다.");
                throw new ApiErrorException(errorResponse.getResponseCode(), errorResponse.getResponseMessage());
            } else {
                log.error("처리되지 않은 API 클라이언트 오류: {}", errorResponse.getResponseCode());
                throw new RuntimeException("API 요청 처리 중 오류가 발생했습니다: " + errorBody);
            }

        } catch (Exception e) {
            log.error("계좌 이체 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("계좌 이체에 실패했습니다.");
        }
    }

    /**
     * 사용자의 전체 계좌 목록을 조회하는 API를 호출합니다.
     *
     * @param userKey 사용자 고유 키
     * @return 조회된 계좌 목록 정보가 담긴 DTO
     */
    public InquireDemandDepositAccountListResponse inquireDemandDepositAccountList(String userKey) {
        // 1. API 요청을 위한 URL과 Body를 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccountList";

        // 이 API는 userKey를 포함하므로, 파라미터가 2개인 createHeader 메서드를 사용합니다.
        RequestHeader header = headerFactory.createHeader("inquireDemandDepositAccountList", userKey);
        InquireDemandDepositAccountListRequest requestBody = new InquireDemandDepositAccountListRequest(header);

        log.info("사용자 전체 계좌 목록 조회 요청: userKey [{}]", userKey);

        try {
            // 2. RestTemplate을 사용하여 POST 요청을 보내고 응답을 받습니다.
            InquireDemandDepositAccountListResponse response = restTemplate.postForObject(url, requestBody, InquireDemandDepositAccountListResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("계좌 목록 조회 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;

        } catch (Exception e) {
            // 3. 에러 발생 시 예외를 처리합니다.
            log.error("계좌 목록 조회 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("계좌 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 계좌 거래 내역을 조회하는 API를 호출합니다.
     *
     * @param userKey         사용자 고유 키
     * @param requestDto      거래 내역 조회에 필요한 요청 데이터 DTO
     * @return 조회된 거래 내역 정보가 담긴 DTO
     */
    /**
     * 계좌 거래 내역을 조회하는 API를 호출합니다.
     *
     * @param userKey         사용자 고유 키
     * @param requestDto      거래 내역 조회에 필요한 요청 데이터 DTO
     * @return 조회된 거래 내역 정보가 담긴 DTO
     */
    public InquireTransactionHistoryListResponse inquireTransactionHistoryList(String userKey, InquireTransactionHistoryListRequestDto requestDto) {
        // 1. API 요청을 위한 URL과 Body를 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/inquireTransactionHistoryList";

        // 이 API는 userKey를 포함하므로, 파라미터가 2개인 createHeader 메서드를 사용합니다.
        RequestHeader header = headerFactory.createHeader("inquireTransactionHistoryList", userKey);

        // 요청 DTO를 생성합니다.
        InquireTransactionHistoryListRequest requestBody = new InquireTransactionHistoryListRequest(
                header,
                requestDto.getAccountNo(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                requestDto.getTransactionType(),
                requestDto.getOrderByType()
        );

        log.info("계좌 거래 내역 조회 요청: 계좌번호 [{}]", requestDto.getAccountNo());

        try {
            // 2. RestTemplate을 사용하여 POST 요청을 보내고 응답을 받습니다.
            InquireTransactionHistoryListResponse response = restTemplate.postForObject(url, requestBody, InquireTransactionHistoryListResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("거래 내역 조회 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;

        } catch (Exception e) {
            // 3. 에러 발생 시 예외를 처리합니다.
            log.error("거래 내역 조회 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("거래 내역 조회에 실패했습니다.");
        }
    }


    /**
     * 1원 송금을 요청하는 외부 금융 API(openAccountAuth)를 호출합니다.
     *
     * @param userKey   사용자 고유 키
     * @param accountNo 인증할 계좌 번호
     * @return 1원 송금 요청 결과가 담긴 DTO
     */
    public void oneWonAuthCall(String userKey, String accountNo) {
        // 1. API 요청을 위한 URL을 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/accountAuth/openAccountAuth";

        // 2. 헤더와 요청 본문을 생성합니다.
        RequestHeader header = headerFactory.createHeader("openAccountAuth", userKey);

        // 요청 DTO를 생성합니다.

        AuthCodeRequest requestBody = new AuthCodeRequest(header, accountNo, authPath);

        log.info("외부 API 1원 송금 요청: 계좌번호 [{}]", accountNo);

        try {
            // 3. RestTemplate을 사용하여 POST 요청을 보냅니다.
            restTemplate.postForObject(url, requestBody, Void.class);

            log.info("외부 API 1원 송금 요청 성공");

        } catch (HttpClientErrorException e) { // 4xx 에러 처리
            String errorBody = e.getResponseBodyAsString();
            log.warn("API 클라이언트 오류: {}, 응답: {}", e.getStatusCode(), errorBody);

            ExceptionResponseDto errorResponse;
            try {
                errorResponse = objectMapper.readValue(errorBody, ExceptionResponseDto.class);
            } catch (Exception parseException) {
                log.error("API 에러 응답 파싱 실패", parseException);
                throw new RuntimeException("API 에러 응답을 파싱할 수 없습니다: " + errorBody);
            }

            // 파싱 성공 후 ApiErrorException으로 변환하여 던집니다.
            log.error("1원 송금 API 호출 실패(HTTP {}) - 코드: {}, 메시지: {}", e.getStatusCode(), errorResponse.getResponseCode(), errorResponse.getResponseMessage());
            throw new ApiErrorException(errorResponse.getResponseCode(), errorResponse.getResponseMessage());

        } catch (Exception e) { // 그 외 모든 예외 처리
            log.error("1원 송금 요청 중 알 수 없는 오류 발생", e);
            throw new RuntimeException("1원 송금 요청에 실패했습니다.");
        }
    }

    /**
     * 사용자가 입력한 1원 이체 인증코드를 확인하는 외부 금융 API를 호출합니다.
     *
     * @param userKey    사용자 고유 키
     * @param accountNo  인증할 계좌 번호
     * @param authCode   사용자가 입력한 인증 코드
     * @return 인증 결과가 담긴 DTO
     */
    public CheckAuthCodeResponse oneWonAuth(String userKey, String accountNo, String authCode) {
        // 1. API 요청을 위한 URL을 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/accountAuth/checkAuthCode";

        // 2. 헤더와 요청 본문을 생성합니다.
        RequestHeader header = headerFactory.createHeader("checkAuthCode", userKey);
        CheckAuthCodeRequest requestBody = new CheckAuthCodeRequest(header, accountNo, authPath, authCode);

        log.info("외부 API 1원 이체 인증코드 확인 요청: 계좌번호 [{}]", accountNo);

        try {
            // 3. RestTemplate을 사용하여 POST 요청을 보냅니다.
            CheckAuthCodeResponse response = restTemplate.postForObject(url, requestBody, CheckAuthCodeResponse.class);
            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }
            log.info("외부 API 1원 이체 인증코드 확인 성공. 응답 코드: {}", response.getHeader().getResponseCode());
            return response;
        } catch (HttpClientErrorException e) { // 4xx 에러 처리
            String errorBody = e.getResponseBodyAsString();
            log.warn("API 클라이언트 오류: {}, 응답: {}", e.getStatusCode(), errorBody);

            ExceptionResponseDto errorResponse;
            try {
                errorResponse = objectMapper.readValue(errorBody, ExceptionResponseDto.class);
            } catch (Exception parseException) {
                log.error("API 에러 응답 파싱 실패", parseException);
                throw new RuntimeException("API 에러 응답을 파싱할 수 없습니다: " + errorBody);
            }

            // 파싱 성공 후 ApiErrorException으로 변환하여 던집니다.
            throw new ApiErrorException(errorResponse.getResponseCode(), errorResponse.getResponseMessage());

        } catch (Exception e) {
            log.error("1원 이체 인증코드 확인 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("1원 이체 인증코드 확인에 실패했습니다.");
        }
    }

    /**
     * 계좌 단건 조회를 요청하는 API를 호출합니다. (새로 추가된 메서드)
     *
     * @param userKey   사용자 고유 키
     * @param accountNo 조회할 계좌 번호
     * @return 조회된 계좌 상세 정보가 담긴 DTO
     */
    public InquireDemandDepositAccountResponse inquireDemandDepositAccount(String userKey, String accountNo) {
        // 1. API 요청을 위한 URL을 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccount";

        // 2. 헤더와 요청 본문을 생성합니다.
        RequestHeader header = headerFactory.createHeader("inquireDemandDepositAccount", userKey);
        InquireDemandDepositAccountRequest requestBody = new InquireDemandDepositAccountRequest(header, accountNo);

        log.info("계좌 단건 조회 요청: 계좌번호 [{}]", accountNo);

        try {
            // 3. RestTemplate을 사용하여 POST 요청을 보냅니다.
            InquireDemandDepositAccountResponse response = restTemplate.postForObject(url, requestBody, InquireDemandDepositAccountResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("계좌 단건 조회 성공");
            return response;

        } catch (HttpClientErrorException e) { // 4xx 에러 처리
            String errorBody = e.getResponseBodyAsString();
            log.warn("API 클라이언트 오류: {}, 응답: {}", e.getStatusCode(), errorBody);

            ExceptionResponseDto errorResponse;
            try {
                errorResponse = objectMapper.readValue(errorBody, ExceptionResponseDto.class);
            } catch (Exception parseException) {
                log.error("API 에러 응답 파싱 실패", parseException);
                throw new RuntimeException("API 에러 응답을 파싱할 수 없습니다: " + errorBody);
            }

            // 파싱 성공 후 ApiErrorException으로 변환하여 던집니다.
            throw new ApiErrorException(errorResponse.getResponseCode(), errorResponse.getResponseMessage());

        } catch (Exception e) { // 그 외 모든 예외 처리
            log.error("계좌 단건 조회 요청 중 알 수 없는 오류 발생", e);
            throw new RuntimeException("계좌 단건 조회 요청에 실패했습니다.");
        }
    }
}
