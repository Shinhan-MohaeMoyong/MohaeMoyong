package shinhan.mohaemoyong.server.oauth2.security.oauth2;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import shinhan.mohaemoyong.server.adapter.user.UserApiAdapter;
import shinhan.mohaemoyong.server.adapter.user.dto.CreateMemberResponse;
import shinhan.mohaemoyong.server.adapter.user.dto.SearchResponse;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.exception.OAuth2AuthenticationProcessingException;
import shinhan.mohaemoyong.server.oauth2.AuthProvider;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.oauth2.security.oauth2.user.OAuth2UserInfo;
import shinhan.mohaemoyong.server.oauth2.security.oauth2.user.OAuth2UserInfoFactory;
import shinhan.mohaemoyong.server.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    private final UserApiAdapter userApiAdapter;

    //백엔드 리다이렉션 페이지에서 토큰을 받은 후 리소스에 다시 요청해서 유저 정보를 받아옴
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, oAuth2User.getAttributes());

        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        // ❗❗❗ 이 부분이 누락되었을 가능성이 높습니다. ❗❗❗
        // UUID 등을 사용하여 고유한 userkey를 생성하고 설정합니다.
        user.setUserkey(oAuth2UserInfo.getId());

        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());

        /*
            userkey 설정
        */
        try {
            // 1) 생성 시도
            CreateMemberResponse response = userApiAdapter.createMember(oAuth2UserInfo.getEmail());

            user.setUserkey(response.getUserKey());


        } catch (RuntimeException ex) {
            // 2) 이미 존재(E4002)면 조회로 폴백
            if ("E4002".equals(ex.getMessage())) {
                SearchResponse existing = userApiAdapter.search(oAuth2UserInfo.getEmail()); // 조회 API
                if (existing == null || !org.springframework.util.StringUtils.hasText(existing.getUserKey())) {
                    throw new IllegalStateException("E4002인데 userKey 조회 실패: " + oAuth2UserInfo.getEmail());
                    /*
                    
                    !!!!!!!!!!! 중복 이메일 예외처리 필요
                    
                    
                     */
                }
                user.setUserkey(existing.getUserKey());
            } else {
                // 3) 다른 에러는 그대로 전파
                throw ex;
            }
        }

        return userRepository.save(user);
    }

    /*
        카카오 계정 이름 변경시 재로그인때 반영
        카카오 계정 프로필사진 변경시 재로그인때 반영
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }

}
