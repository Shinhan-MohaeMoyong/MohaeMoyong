package shinhan.mohaemoyong.server.adapter.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shinhan.mohaemoyong.server.adapter.common.dto.Header;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class HeaderFactory { 
    // application.properties에서 고정값 설정
    @Value("${api.shinhan.institution-code}")
    private String institutionCode;

    @Value("${api.shinhan.fintech-app-no}")
    private String fintechAppNo;

    @Value("${api.shinhan.api-key}")
    private String apiKey;

    /**
     * API 요청에 필요한 공통 헤더를 생성합니다.
     * @param apiName 요청할 API의 이름 (예: "drawingTransfer")
     * @param userKey 사용자의 고유 키
     * @return 완성된 Header 객체
     */
    public Header createHeader(String apiName, String userKey) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 날짜와 시간을 API 형식에 맞게 포맷팅합니다.
        String transmissionDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String transmissionTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));

        // 기관거래고유번호 생성 (기관코드 + 생성일시 + 6자리 난수)
        String uniqueNo = institutionCode + transmissionDate + transmissionTime +
                ThreadLocalRandom.current().nextInt(100000, 1000000);

        return Header.builder()
                .apiName(apiName)
                .transmissionDate(transmissionDate)
                .transmissionTime(transmissionTime)
                .institutionCode(institutionCode)
                .fintechAppNo(fintechAppNo)
                .apiServiceCode(apiName) // apiServiceCode는 apiName과 동일
                .institutionTransactionUniqueNo(uniqueNo)
                .apiKey(apiKey)
                .userKey(userKey)
                .build();
    }
}
