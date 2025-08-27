package shinhan.mohaemoyong.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
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

        // 에러 코드에 따라 적절한 HTTP 상태 코드를 반환
        HttpStatus status;
        switch (e.getErrorCode()) {
            case "E001": // 권한 없음
                status = HttpStatus.FORBIDDEN; // 403
                break;
            case "A1014": // 특정 금융 API 에러
                status = HttpStatus.BAD_REQUEST; // 400
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
                break;
        }

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleException(Exception e) {
        if (e instanceof org.springframework.web.server.ResponseStatusException rse) {
            // 스프링 기본 흐름(or 위의 핸들러)으로 처리되게 그대로 던짐
            throw rse;
        }

        log.error("처리되지 않은 예외 발생", e); // 스택 트레이스를 모두 보려면 e를 출력

        ExceptionResponseDto errorResponse = ExceptionResponseDto.builder()
                .responseCode("INTERNAL_SERVER_ERROR")
                .responseMessage("서버 내부 오류가 발생했습니다.")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponseDto> handleRSE(ResponseStatusException e) {
        log.warn("RSE 발생: status={}, reason={}", e.getStatusCode(), e.getReason());

        ExceptionResponseDto body = ExceptionResponseDto.builder()
                .responseCode(e.getStatusCode().toString())
                .responseMessage(e.getReason() != null ? e.getReason() : "요청이 거부되었습니다.")
                .build();

        return new ResponseEntity<>(body, e.getStatusCode());
    }
}
