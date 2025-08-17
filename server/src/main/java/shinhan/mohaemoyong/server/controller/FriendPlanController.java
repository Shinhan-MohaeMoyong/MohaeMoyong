package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.FriendPlanService;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendPlanController {

    private final FriendPlanService friendPlanService;

    /** 친구가 이번주 새 일정 등록했는지 확인 (빨간테두리 여부) */
    @GetMapping("/{friendId}/plans/new")
    public boolean hasNewPlanThisWeek(@CurrentUser UserPrincipal userPrincipal,
                                      @PathVariable Long friendId) {
        return friendPlanService.hasNewPlanThisWeek(userPrincipal.getId(), friendId);
    }

    /** 친구 일정 확인 완료 (빨간테두리 제거) */
    @PostMapping("/{friendId}/plans/seen")
    public void markPlansAsSeen(@CurrentUser UserPrincipal userPrincipal,
                                @PathVariable Long friendId) {
        friendPlanService.markPlansAsSeen(userPrincipal.getId(), friendId);
    }
}
