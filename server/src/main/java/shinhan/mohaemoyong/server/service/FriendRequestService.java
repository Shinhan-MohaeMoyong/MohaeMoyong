package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.domain.FriendRequest;
import shinhan.mohaemoyong.server.domain.Friendship;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.dto.FriendRequestDto;
import shinhan.mohaemoyong.server.dto.FriendRequestResponse;
import shinhan.mohaemoyong.server.dto.FriendResponse;
import shinhan.mohaemoyong.server.exception.ResourceNotFoundException;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.repository.FriendRequestRepository;
import shinhan.mohaemoyong.server.repository.FriendshipRepository;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /** 친구 요청 보내기 */
    @Transactional
    public FriendRequest sendRequest(UserPrincipal userPrincipal, FriendRequestDto dto) {
        User requester = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("요청자 유저 없음"));
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("받는 사람 유저 없음"));

        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }

        boolean alreadyPending = friendRequestRepository
                .existsByRequesterAndReceiverAndStatus(requester, receiver, FriendRequest.Status.PENDING);
        if (alreadyPending) {
            throw new IllegalStateException("이미 보낸 요청이 있습니다.");
        }

        FriendRequest request = new FriendRequest();
        request.setRequester(requester);
        request.setReceiver(receiver);
        request.setMessage(dto.getMessage());

        return friendRequestRepository.save(request);
    }

    /** 받은 요청 목록 조회 */
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getReceivedRequests(UserPrincipal userPrincipal) {
        User receiver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<FriendRequest> requests =
                friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequest.Status.PENDING);

        return requests.stream()
                .map(FriendRequestResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 보낸 요청 목록 조회 */
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getSentRequests(UserPrincipal userPrincipal) {
        var requests = friendRequestRepository.findByRequester_IdAndIsActiveTrue(userPrincipal.getId());
        return requests.stream()
                .map(FriendRequestResponse::fromEntity)   // from() 말고 fromEntity로 통일 권장
                .toList();
    }


    /** 친구 요청 수락 */
    @Transactional
    public FriendResponse acceptRequest(Long userId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("FriendRequest", "id", requestId));

        // 수신자가 본인인지 검증
        if (!request.getReceiver().getId().equals(userId)) {
            throw new AccessDeniedException("본인에게 온 요청만 수락할 수 있습니다.");
        }

        request.setStatus(FriendRequest.Status.ACCEPTED);
        request.setRespondedAt(Instant.now());

        // Friendship 저장 (양방향으로 저장하는 게 안전)
        Friendship f1 = new Friendship();
        f1.setUser(request.getRequester());
        f1.setFriend(request.getReceiver());

        Friendship f2 = new Friendship();
        f2.setUser(request.getReceiver());
        f2.setFriend(request.getRequester());

        friendshipRepository.save(f1);
        friendshipRepository.save(f2);

        // Response로 변환
        return FriendResponse.builder()
                .id(request.getRequester().getId())
                .name(request.getRequester().getName())
                .email(request.getRequester().getEmail())
                .imageUrl(request.getRequester().getImageUrl())
                .build();
    }

    /** 친구 요청 거절 */
    @Transactional
    public void declineRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("FriendRequest", "id", requestId));

        if (!request.getReceiver().getId().equals(userId)) {
            throw new AccessDeniedException("해당 요청을 거절할 권한이 없습니다.");
        }

        if (request.getStatus() != FriendRequest.Status.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        request.setStatus(FriendRequest.Status.DECLINED);
        request.setRespondedAt(Instant.now());
        friendRequestRepository.save(request);
    }

    /** 보낸 요청 취소 */
    @Transactional
    public void cancelRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("FriendRequest", "id", requestId));

        if (!request.getRequester().getId().equals(userId)) {
            throw new AccessDeniedException("자신이 보낸 요청만 취소할 수 있습니다.");
        }

        if (request.getStatus() != FriendRequest.Status.PENDING) {
            throw new IllegalStateException("이미 처리된 요청은 취소할 수 없습니다.");
        }

        request.setStatus(FriendRequest.Status.CANCELED);
        request.setIsActive(false);
        request.setRespondedAt(Instant.now());

        friendRequestRepository.save(request);
    }

}
