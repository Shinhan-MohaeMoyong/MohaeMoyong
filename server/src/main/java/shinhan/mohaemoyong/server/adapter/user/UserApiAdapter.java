package shinhan.mohaemoyong.server.adapter.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import shinhan.mohaemoyong.server.adapter.user.dto.*;

@Slf4j
@Component
public class UserApiAdapter {
    private final RestTemplate restTemplate;

    // application.properties 파일에서 설정 정보 가져오기
    @Value("${api.shinhan.base-url}")
    private String baseUrl;

    @Value("${api.shinhan.api-key}")
    private String apiKey;

    // 생성자를 통해 RestTemplate Bean을 주입받습니다.
    public UserApiAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    /**
     * 신한은행 금융 API를 사용하기 위한 API KEY를 발급받는 메서드
     *
     * @param managerId 관리자 ID (이메일)
     * @return 발급된 API 키 정보가 담긴 DTO
     * @throws RuntimeException API 호출 실패 시 발생
     */
    public IssuedApiKeyResponse issueApiKey(String managerId) {
        String url = baseUrl + "/ssafy/api/v1/edu/app/issuedApiKey";
        IssuedApiKeyRequest requestBody = new IssuedApiKeyRequest(managerId);

        log.info("신한은행 API KEY 발급 요청: {}", managerId);

        try {
            IssuedApiKeyResponse response = restTemplate.postForObject(url, requestBody, IssuedApiKeyResponse.class);

            if (response == null || response.getApiKey() == null) {
                throw new RuntimeException("API 키 발급 응답이 비어있습니다.");
            }

            log.info("신한은행 API KEY 발급 성공. 만료일: {}", response.getExpirationDate());
            return response;

        } catch (Exception e) {
            log.error("신한은행 API KEY 발급 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("API 키 발급에 실패했습니다.");
        }
    }

    /**
     * 신한은행 금융망에 신규 사용자 계정을 생성하는 API를 호출합니다.
     *
     * @param userId 생성할 사용자의 ID (이메일)
     * @return 생성된 사용자의 정보가 담긴 DTO
     */
    public CreateMemberResponse createMember(String userId) {
        String url = baseUrl + "/ssafy/api/v1/member/";
        CreateMemberRequest requestBody = new CreateMemberRequest(apiKey, userId);

        try {
            CreateMemberResponse resp = restTemplate.postForObject(url, requestBody, CreateMemberResponse.class);
            if (resp == null) throw new RuntimeException("API 응답이 비어있습니다.");
            return resp;

        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();  // 에러 응답 JSON
            if (body.equals("E4002")) {
                // 이미 존재하는 ID → 여기서 바로 처리(예: 조회 폴백 or 전용 예외 던지기)
                log.info("E4002: 이미 존재하는 사용자. userId={}", userId);

                // 호출부에서 처리하도록 전용 예외 던지기
                throw new RuntimeException("E4002"); // 최소 변경
            }
            throw e; // 다른 400대 같은거는 그대로 전파

        } catch (Exception e) {
            log.error("사용자 계정 생성 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("사용자 계정 생성에 실패했습니다.", e);
        }
    }



    /**
     * 신한은행 금융 API를 호출하여 사용자 정보를 조회하는 메서드
     *
     * @param userId 조회할 사용자의 이메일 주소
     * @return 조회된 사용자 정보 DTO
     * @throws RuntimeException API 호출 실패 시 발생
     */
    public SearchResponse search(String userId) {
        // 1. API 요청을 위한 URL과 Body를 준비합니다.
        String url = baseUrl + "/ssafy/api/v1/member/search";
        SearchRequest requestBody = new SearchRequest(userId, apiKey);

        log.info("신한은행 API 사용자 정보 조회 요청: {}", userId);

        try {
            // 2. RestTemplate을 사용하여 POST 요청을 보내고 응답을 받습니다.
            SearchResponse response = restTemplate.postForObject(url, requestBody, SearchResponse.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            log.info("신한은행 API 응답 성공: {}", response.getUserId());
            return response;

        } catch (Exception e) {
            // 3. API가 4xx 에러(E4003, E4004 등)를 반환했을 때 예외를 처리합니다. [cite: 1]
            log.error("신한은행 API 사용자 정보 조회 실패. 에러: {}", e.getMessage());
            throw new RuntimeException("사용자 정보 조회에 실패했습니다.");
        }
    }
}
