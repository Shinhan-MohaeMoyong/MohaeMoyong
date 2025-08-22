package shinhan.mohaemoyong.server.adapter.user;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.HandlerMapping;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.CreateDemandDepositAccountResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.InquireDemandDepositListResponse;
import shinhan.mohaemoyong.server.adapter.user.dto.CreateMemberResponse;

import java.util.List;


@Slf4j
@SpringBootTest // Spring Boot 환
public class UserApiAdapterTest {
    @Autowired // Spring 컨테이너에 등록된 실제 DemandDepositApiAdapter Bean을 주입받음
    private UserApiAdapter userApiAdapter;
    @Autowired
    private HandlerMapping resourceHandlerMapping;


    @Test
    @DisplayName("사용자계정생성_API_호출_테스트")
    void 사용자계정생성_API_호출_테스트(){
        // 계정 생성을 위해 userId(이메일)을 파라미터로 createMember 호출
        String userId = "chw030700@gmail.com";
        CreateMemberResponse response = userApiAdapter.createMember(userId);

        // 1. 응답에서 userKey 정보을 가져옵니다.
        log.info("userKey : {}", response.getUserKey());
    }
}
