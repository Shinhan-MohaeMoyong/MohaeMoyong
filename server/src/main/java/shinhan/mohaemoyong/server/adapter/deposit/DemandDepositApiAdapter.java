package shinhan.mohaemoyong.server.adapter.deposit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;
import shinhan.mohaemoyong.server.adapter.deposit.dto.CreateDemandDepositRequest;
import shinhan.mohaemoyong.server.adapter.deposit.dto.CreateDemandDepositResponse;
import shinhan.mohaemoyong.server.adapter.common.factory.HeaderFactory;

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
}
