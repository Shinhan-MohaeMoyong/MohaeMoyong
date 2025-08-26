package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinhan.mohaemoyong.server.domain.FriendRequest;
import shinhan.mohaemoyong.server.dto.FriendRequestDto;
import shinhan.mohaemoyong.server.dto.FriendRequestResponse;
import shinhan.mohaemoyong.server.dto.FriendResponse;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.FriendService;
import shinhan.mohaemoyong.server.service.FriendRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;             // 친구 목록 관리
    private final FriendRequestService friendRequestService; // 친구 요청 관리

    /** 내 친구 목록 조회 */
    @GetMapping
    public List<FriendResponse> getFriends(@CurrentUser UserPrincipal userPrincipal) {
        return friendService.getAllFriends(userPrincipal);
    }

    /** 친구 요청 보내기 */
    @PostMapping("/requests")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody FriendRequestDto dto) {

        FriendRequest request = friendRequestService.sendRequest(userPrincipal, dto);
        return new ResponseEntity<>(FriendRequestResponse.from(request), HttpStatus.CREATED);
    }

    /** 받은 친구 요청 목록 조회 */
    @GetMapping("/requests/inbox")
    public ResponseEntity<List<FriendRequestResponse>> getInbox(@CurrentUser UserPrincipal userPrincipal) {
        return ResponseEntity.ok(friendRequestService.getReceivedRequests(userPrincipal));
    }

    /** 보낸 친구 목록 조회 **/
    @GetMapping("/requests/outbox")
    public ResponseEntity<List<FriendRequestResponse>> getOutbox(@CurrentUser UserPrincipal userPrincipal) {
        return ResponseEntity.ok(friendRequestService.getSentRequests(userPrincipal)); // ✅ 여기!
    }

    /** 친구 요청 수락 */
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendResponse> acceptRequest(
            @PathVariable Long requestId,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(friendRequestService.acceptRequest(userPrincipal.getId(), requestId));
    }

    /** 친구 요청 거절 */
    @PostMapping("/requests/{requestId}/decline")
    public ResponseEntity<String> declineRequest(@PathVariable Long requestId,
                                                 @CurrentUser UserPrincipal userPrincipal) {
        friendRequestService.declineRequest(requestId, userPrincipal.getId());
        return ResponseEntity.ok("친구 요청이 거절되었습니다.");
    }

    /** 보낸 요청 취소 */
    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long requestId,
            @CurrentUser UserPrincipal userPrincipal) {
        friendRequestService.cancelRequest(requestId, userPrincipal.getId());
        return ResponseEntity.ok().build(); // 200 OK만 응답
    }

    // 친구 삭제
    @DeleteMapping("/{friendId}")
    public ResponseEntity<String> deleteFriend(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long friendId) {
        friendService.deleteFriend(userPrincipal.getId(), friendId);
        return ResponseEntity.ok("친구가 삭제되었습니다.");
    }
}
