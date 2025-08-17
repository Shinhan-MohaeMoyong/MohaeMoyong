package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.dto.DetailPlanResponse;
import shinhan.mohaemoyong.server.dto.DetailPlanUpdateRequest;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.DetailPlanService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plans")
public class DetailPlanController {

    private final DetailPlanService detailPlanService;

    // GET /api/v1/plans/{planId}
    @GetMapping("/{planId}")
    public ResponseEntity<DetailPlanResponse> getDetail(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long planId
    ) {
        Long loginUserId = userPrincipal.getId();
        return ResponseEntity.ok(detailPlanService.getDetail(loginUserId, planId));
    }

    // PATCH /api/v1/plans/{planId}
    @PatchMapping("/{planId}")
    public ResponseEntity<DetailPlanResponse> updateDetail(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long planId,
            @RequestBody DetailPlanUpdateRequest request
    ) {
        Long loginUserId = userPrincipal.getId();
        return ResponseEntity.ok(detailPlanService.update(loginUserId, planId, request));
    }

    // DELETE /api/v1/plans/{planId}
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> delete(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long planId
    ) {
        Long loginUserId = userPrincipal.getId();
        detailPlanService.delete(loginUserId, planId);
        return ResponseEntity.noContent().build();
    }
}
