package shinhan.mohaemoyong.server.oauth2.security.oauth2;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import shinhan.mohaemoyong.server.adapter.user.UserApiAdapter;
import shinhan.mohaemoyong.server.adapter.user.dto.CreateMemberResponse;
import shinhan.mohaemoyong.server.adapter.user.dto.SearchResponse;
import shinhan.mohaemoyong.server.domain.User;
import shinhan.mohaemoyong.server.adapter.exception.ApiErrorException;
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

    /*
        OAuth2 로 부터 받은 데이터를 토대로 우리 서버의 비지니스 로직에 적용하는 메서드

        - 금융망 계정 조회 api를 호출하여 userkey를 받을 수 있는지 체크
            - 없다면 : 금융망 계정 생성 API를 통해 userkey 받음
            - 있다면 : 그 응답의 userkey를 받음

        - 사용자가 이미 우리 DB에 등록된 사용자인지 확인
            - 이미 등록된 사용자다 -> updateExistingUser() : 이름, 프로필사진, 받은 userkey로 userkey컬럼을 업데이트
            - 이미 등록된 사용자가 아니다 -> registerNewUser() : 우리DB의 Users 레코드 생성 (받은 userkey 활용)
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, oAuth2User.getAttributes());

        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail()); // 엔티티 꺼내고 null일수 있으니 옵셔널
        User user;

        String userkey;

        try { // 금융망 계정 조회 api를 호출하여 userkey를 받을 수 있는지 체크

            SearchResponse response = userApiAdapter.search(oAuth2UserInfo.getEmail());
            userkey = response.getUserKey();

        } catch (ApiErrorException ex) {

            if ("E4003".equals(ex.getErrorCode())) { // 없다면 : 금융망 계정 생성 API를 통해 userkey 받음

                try {
                    // 1) 생성 시도
                    CreateMemberResponse response = userApiAdapter.createMember(oAuth2UserInfo.getEmail());
                    userkey = response.getUserKey();

                } catch (ApiErrorException ex2) { // 이메일 중복 관련 이므로 현재는 무시 가능
                    // 2) 이미 존재된 email이면 (E4002)
                    if ("E4002".equals(ex2.getErrorCode())) {
                        //이미 등록된 이메일 : 이메일이 중복이라고 예외를 프런트에 응답
                        throw new ResponseStatusException( // 커스텀바디 X
                                HttpStatus.CONFLICT, // 409
                                ex.getErrorCode() + " : " + ex.getErrorMessage()
                        );

                    } else {
                        // 다른 에러는 일단은 그대로 전파
                        throw ex2;
                    }
                }
            } else {
                // 다른 에러는 일단은 그대로 전파
                throw ex;
            }
        }

        if(userOptional.isPresent()) { // 이미 등록된 사용자다
            user = userOptional.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo, userkey);
        } else { // 이미 등록된 사용자가 아니다
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo, userkey);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo, String userkey) {
        User user = new User();

        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setUserkey(userkey);


        return userRepository.save(user);
    }

    /*
        이미 존재하는 유저인데 카카오 계정 이름 변경시 재로그인때 반영
        이미 존재하는 유저인데 카카오 계정 프로필사진 변경시 재로그인때 반영

        이미 존재하는 유저인데 만약 userkey가 없다면 조회 후 반영
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, String userkey) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());

        existingUser.setUserkey(userkey);

        return userRepository.save(existingUser);
    }

}
