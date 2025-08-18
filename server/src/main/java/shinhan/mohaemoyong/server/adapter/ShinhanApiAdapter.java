package shinhan.mohaemoyong.server.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import shinhan.mohaemoyong.server.adapter.factory.HeaderFactory;

/**
 * 신한은행 외부 금융 API와의 통신을 전담하는 어댑터 클래스
 */
@Slf4j
@Component
public class ShinhanApiAdapter {
    private final RestTemplate restTemplate;
    private final HeaderFactory headerFactory;

    // application.properties 파일에서 설정 정보 가져오기
    @Value("${api.shinhan.base-url}")
    private String baseUrl;

    @Value("${api.shinhan.api-key}")
    private String apiKey;

    // 생성자를 통해 RestTemplate Bean을 주입받습니다.
    public ShinhanApiAdapter(RestTemplate restTemplate, HeaderFactory headerFactory) {
        this.restTemplate = restTemplate;
        this.headerFactory = headerFactory;
    }
}







}