package shinhan.mohaemoyong.server.adapter.exception;

import lombok.Getter;

// 금융 API 특유의 에러코드 예외 정의
@Getter
public class ApiErrorException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public ApiErrorException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


}
