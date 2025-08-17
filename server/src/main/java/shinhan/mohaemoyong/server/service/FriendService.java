package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinhan.mohaemoyong.server.dto.FriendResponse;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;

    public List<FriendResponse> getAllFriends() {
        return userRepository.findAll().stream()
                .map(user -> FriendResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .imageUrl(user.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
