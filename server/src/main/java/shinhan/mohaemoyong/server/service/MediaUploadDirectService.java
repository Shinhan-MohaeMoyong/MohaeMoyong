package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUploadDirectService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucket;

    @Value("${app.upload.allowedMime:image/jpeg,image/png,image/webp}")
    private String allowedMimeCsv;

    @Value("${app.upload.maxBytes:10485760}")
    private long maxBytes;

    @Value("${app.upload.basePrefix:comments}")
    private String basePrefix;

    @Value("${app.upload.cdnBaseUrl:}")
    private String cdnBaseUrl;

    public UploadResult upload(MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        String contentType = file.getContentType();
        long size = file.getSize();

        // 간단 검증
        Set<String> allowed = Set.of(allowedMimeCsv.split(","));
        if (contentType == null || !allowed.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 MIME: " + contentType);
        }
        if (size <= 0 || size > maxBytes) {
            throw new IllegalArgumentException("파일 크기 초과 (max=" + maxBytes + "): " + size);
        }

        // 바이트로 읽어서 (토이용: 메모리에 한번 올림) → width/height 추출 + 업로드 재사용
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalStateException("파일 읽기에 실패했습니다.", e);
        }

        Integer w = null, h = null;
        if (contentType.startsWith("image/")) {
            try (var bais = new ByteArrayInputStream(bytes)) {
                BufferedImage img = ImageIO.read(bais);
                if (img != null) { w = img.getWidth(); h = img.getHeight(); }
            } catch (Exception ignored) {}
        }

        // objectKey 생성: comments/yyyy/MM/dd/uuid.ext
        String today = LocalDate.now().toString().replace("-", "/");
        String ext = guessExt(contentType, file.getOriginalFilename());
        String objectKey = "%s/%s/%s.%s".formatted(basePrefix, today, UUID.randomUUID(), ext);

        // S3 업로드
        var put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

        try {
            s3Client.putObject(put, RequestBody.fromBytes(bytes));
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            // S3에서 오는 상세 에러 로그 확인
            System.err.println("S3Error: status=" + e.statusCode()
                    + ", code=" + (e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "unknown")
                    + ", msg=" + e.getMessage());
            throw e; // 일단 다시 던져서 컨트롤러까지 전달
        }


        // 최종 URL 구성
        String url = buildPublicUrl(objectKey);

        return new UploadResult(url, contentType, size, w, h, objectKey);
    }

    private String guessExt(String contentType, String originalName) {
        if (contentType != null) {
            switch (contentType) {
                case "image/jpeg": return "jpg";
                case "image/png" : return "png";
                case "image/webp": return "webp";
            }
        }
        if (originalName != null) {
            int i = originalName.lastIndexOf('.');
            if (i > -1 && i < originalName.length() - 1) return originalName.substring(i + 1);
        }
        return "bin";
    }

    private String buildPublicUrl(String objectKey) {
        if (cdnBaseUrl == null || cdnBaseUrl.isBlank()) {
            return "https://" + bucket + ".s3.amazonaws.com/" + objectKey;
        }
        String base = cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length()-1) : cdnBaseUrl;
        return base + "/" + objectKey;
    }

    // 간단 응답 DTO
    public record UploadResult(
            String url, String contentType, long size,
            Integer width, Integer height, String objectKey
    ) {}
}