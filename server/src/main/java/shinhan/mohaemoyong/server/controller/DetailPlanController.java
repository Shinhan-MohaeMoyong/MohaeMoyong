package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.dto.DetailPlanResponse;
import shinhan.mohaemoyong.server.service.DetailPlanService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/plans")
public class DetailPlanController {

    private final DetailPlanService detailPlanService;

    // GET /api/users/{userId}/plans/{planId}
    @GetMapping("/{planId}")
    public ResponseEntity<DetailPlanResponse> getDetail(
            @PathVariable Long userId,
            @PathVariable Long planId
    ) {
        return ResponseEntity.ok(detailPlanService.getDetail(userId, planId));
    }
}
