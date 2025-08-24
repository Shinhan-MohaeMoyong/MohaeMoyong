package shinhan.mohaemoyong.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
import shinhan.mohaemoyong.server.adapter.exception.ExceptionResponseDto;

@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ExceptionResponseDto> handleApiErrorException(ApiErrorException e) {
        log.warn("API 에러 발생: 코드 [{}], 메시지 [{}]", e.getErrorCode(), e.getErrorMessage());

        ExceptionResponseDto errorResponse = ExceptionResponseDto.builder()
                .responseCode(e.getErrorCode())
                .responseMessage(e.getErrorMessage())
                .build();

        HttpStatus status = "A1014".equals(e.getErrorCode())
                ? HttpStatus.BAD_REQUEST // 400
                : HttpStatus.INTERNAL_SERVER_ERROR; // 500

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생", e); // 스택 트레이스를 모두 보려면 e를 출력

        ExceptionResponseDto errorResponse = ExceptionResponseDto.builder()
                .responseCode("INTERNAL_SERVER_ERROR")
                .responseMessage("서버 내부 오류가 발생했습니다.")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
