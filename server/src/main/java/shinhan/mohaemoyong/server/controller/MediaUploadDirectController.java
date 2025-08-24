package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shinhan.mohaemoyong.server.oauth2.security.CurrentUser;
import shinhan.mohaemoyong.server.oauth2.security.UserPrincipal;
import shinhan.mohaemoyong.server.service.MediaUploadDirectService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads/media")
public class MediaUploadDirectController {

    private final MediaUploadDirectService service;
    @Value("${app.upload.maxCount:5}")
    private int maxCount;

    // Multipart 업로드 (form-data)
    @PostMapping(value = "/direct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadDirect(
            @RequestParam("file") MultipartFile file,
            @CurrentUser UserPrincipal user
    ) {
        var res = service.upload(file, user.getId());
        return ResponseEntity.ok(res);
    }


    // 2) 다중 파일 업로드
    @PostMapping(value = "/direct/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MediaUploadDirectService.UploadResult>> uploadDirectBatch(
            @RequestParam("files") List<MultipartFile> files,
            @CurrentUser UserPrincipal user
    ) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (files.size() > maxCount) {
            throw new IllegalArgumentException("최대 " + maxCount + "개까지 업로드할 수 있습니다.");
        }

        Long uploaderId = user.getId();

        // 순차 처리
        List<MediaUploadDirectService.UploadResult> results = files.stream()
                .map(f -> service.upload(f, uploaderId))
                .toList();

        return ResponseEntity.ok(results);
    }
}
