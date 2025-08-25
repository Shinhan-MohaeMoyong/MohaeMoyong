package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import shinhan.mohaemoyong.server.dto.User.UserResponse;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.UserService;

import java.util.List;


@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @GetMapping("/user/me")
    public ResponseEntity<UserResponse> findUserDetail(@CurrentUser UserPrincipal userPrincipal) {

        return new ResponseEntity<>(userService.getUserDetail(userPrincipal), HttpStatus.OK);
    }

    @GetMapping("/api/v1/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}