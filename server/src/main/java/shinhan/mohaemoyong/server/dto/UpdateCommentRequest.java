package shinhan.mohaemoyong.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCommentRequest {

    @Size(max = 2000, message = "댓글은 2000자를 초과할 수 없습니다.")
    private String content;

    // 선택: 새로 추가할 이미지 URL들 (S3 업로드 결과)
    private List<String> addImageUrls;

    // 선택: 삭제할 댓글-이미지 row의 PK들
    private List<Long> removeImageIds;

    // getter/setter
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getAddImageUrls() { return addImageUrls; }
    public void setAddImageUrls(List<String> addImageUrls) { this.addImageUrls = addImageUrls; }
    public List<Long> getRemoveImageIds() { return removeImageIds; }
    public void setRemoveImageIds(List<Long> removeImageIds) { this.removeImageIds = removeImageIds; }
}
