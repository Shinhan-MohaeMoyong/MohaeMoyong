package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.dto.HomeWeekResponse;
import shinhan.mohaemoyong.server.service.HomeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    // GET /api/v1/home/plans/week?userId=1
    @GetMapping("/plans/week")
    public List<HomeWeekResponse> getThisWeekPlans(@RequestParam Long userId) {
        return homeService.getThisWeekPlans(userId);
    }
}
