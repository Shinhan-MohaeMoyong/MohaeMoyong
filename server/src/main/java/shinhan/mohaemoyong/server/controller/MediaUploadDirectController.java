package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.MediaUploadDirectService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads/media")
public class MediaUploadDirectController {

    private final MediaUploadDirectService service;

    // Multipart 업로드 (form-data)
    @PostMapping(value = "/direct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDirect(
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal user
    ) {
        var res = service.upload(file, user.getId());
        return ResponseEntity.ok(res);
    }
}
