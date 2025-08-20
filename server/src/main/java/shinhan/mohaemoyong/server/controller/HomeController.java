package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.HomeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    // GET /api/v1/home/plans/week/myPlans
    // ✅ 내 일정 조회 (기존)
    @GetMapping("/plans/week/myPlans")
    public List<HomeWeekResponse> getThisWeekPlans(@CurrentUser UserPrincipal userPrincipal) {
        return homeService.getThisWeekPlans(userPrincipal.getId());
    }

    // GET /api/v1/home/plans/myPlans
    // 내 모든 일정 조회
    @GetMapping("/plans/my")
    public List<HomeWeekResponse> getAllPlans(@CurrentUser UserPrincipal userPrincipal) {
        return homeService.getAllPlans(userPrincipal.getId());
    }
}
