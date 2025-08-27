package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.FriendRequest;
import shinhan.mohaemoyong.server.domain.Friendship;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.FriendResponse;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.FriendRequestRepository;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    /** 현재 로그인 유저의 친구 목록 조회 */
    @Transactional(readOnly = true)
    public List<FriendResponse> getAllFriends(UserPrincipal userPrincipal) {
        User me = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userPrincipal.getId()));

        List<Friendship> friendships = friendshipRepository.findByUser(me);

        return friendships.stream()
                .map(f -> {
                    User friend = f.getFriend();
                    return FriendResponse.builder()
                            .id(friend.getId())
                            .name(friend.getName())
                            .email(friend.getEmail())
                            .imageUrl(friend.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 친구 삭제
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found"));

        // ✅ 양방향 존재 여부로 체크
        if (!friendshipRepository.existsFriendEdge(userId, friendId)) {
            throw new IllegalStateException("친구 관계가 존재하지 않습니다.");
        }

        // 1) 친구 관계 양방향 삭제
        friendshipRepository.deletePair(user, friend);

        // 2) 두 사람 사이의 모든 친구요청 비활성/취소 (또는 물리삭제)
        friendRequestRepository.cancelAllBetweenByIds(userId, friendId, FriendRequest.Status.CANCELED);
        // friendRequestRepository.deleteAllBetween(user, friend); // 물리삭제를 원하면 이걸 사용
    }

    //친구 요청 검색 창
    public List<FriendResponse> searchAvailableUsers(Long myId, String query) {
        log.info("searchAvailableUsers myId = {}", myId);
        log.info("searchAvailableUsers query = {}", query);

        List<User> users = userRepository.searchAvailableUsers(myId, query);
        return users.stream()
                .map(FriendResponse::from) // FriendResponse DTO 변환
                .toList();
    }

}
