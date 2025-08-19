package shinhan.mohaemoyong.server.adapter.common.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shinhan.mohaemoyong.server.adapter.common.headerDto.RequestHeader;

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
     * API 요청에 필요한 공통 헤더를 생성합니다. (userKey 포함)
     * @param apiName 요청할 API의 이름
     * @param userKey 사용자의 고유 키
     * @return 완성된 RequestHeader 객체
     */
    public RequestHeader createHeader(String apiName, String userKey) {
        String uniqueNo = createUniqueNo();
        String transmissionDate = getTransmissionDate();
        String transmissionTime = getTransmissionTime();

        return RequestHeader.builder()
                .apiName(apiName)
                .transmissionDate(transmissionDate)
                .transmissionTime(transmissionTime)
                .institutionCode(institutionCode)
                .fintechAppNo(fintechAppNo)
                .apiServiceCode(apiName)
                .institutionTransactionUniqueNo(uniqueNo)
                .apiKey(apiKey)
                .userKey(userKey)
                .build();
    }

    /**
     * API 요청에 필요한 공통 헤더를 생성합니다. (userKey 제외)
     * @param apiName 요청할 API의 이름
     * @return 완성된 RequestHeader 객체
     */
    public RequestHeader createHeader(String apiName) {
        String uniqueNo = createUniqueNo();
        String transmissionDate = getTransmissionDate();
        String transmissionTime = getTransmissionTime();

        return RequestHeader.builder()
                .apiName(apiName)
                .transmissionDate(transmissionDate)
                .transmissionTime(transmissionTime)
                .institutionCode(institutionCode)
                .fintechAppNo(fintechAppNo)
                .apiServiceCode(apiName)
                .institutionTransactionUniqueNo(uniqueNo)
                .apiKey(apiKey)
                // userKey 필드를 설정하지 않습니다.
                .build();
    }

    // 중복 코드를 줄이기 위한 private 헬퍼 메서드
    private String getTransmissionDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String getTransmissionTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
    }

    private String createUniqueNo() {
        return institutionCode + getTransmissionDate() + getTransmissionTime() +
                ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}