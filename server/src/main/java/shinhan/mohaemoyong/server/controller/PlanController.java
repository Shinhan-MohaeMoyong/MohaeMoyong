package shinhan.mohaemoyong.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.DetailOneDayPlanResponse;
import shinhan.mohaemoyong.server.dto.DetailPlanResponse;
import shinhan.mohaemoyong.server.dto.PlanCreateRequest;
import shinhan.mohaemoyong.server.dto.PlanCreateResponse;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.PlanService;

import java.time.LocalDate;
import java.util.List;

// ✅ 일정 관련 API 컨트롤러
@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /**
     * 일정 추가 API
     * POST /api/v1/plans
     */
    @PostMapping
    public ResponseEntity<PlanCreateResponse> createPlan(
            @AuthenticationPrincipal UserPrincipal user,   // ✅ User → UserPrincipal
            @Valid @RequestBody PlanCreateRequest request) {

        Long creatorId = user.getId();   // 이제 오류 없이 동작
        PlanCreateResponse response = planService.createPlan(creatorId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{date}/myPlans")
    public ResponseEntity<List<DetailOneDayPlanResponse>> getPlansByDate(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){

        Long userId = userPrincipal.getId();
        List<DetailOneDayPlanResponse> plans = planService.selectPlansByDate(date, userId);
        return new ResponseEntity<>(plans, HttpStatus.OK);
    }
}
