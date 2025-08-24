package shinhan.mohaemoyong.server.adapter.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExceptionResponseDto {
    private String responseCode;
    private String responseMessage;

}
