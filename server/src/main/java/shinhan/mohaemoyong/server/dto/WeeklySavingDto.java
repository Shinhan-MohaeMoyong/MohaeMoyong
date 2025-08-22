package shinhan.mohaemoyong.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklySavingDto {
    private String weekDescription;
    private Long amount;
}