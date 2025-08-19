package shinhan.mohaemoyong.server.adapter.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    // 요청 메시지 명세
    private String userId;
    private String apiKey;
}