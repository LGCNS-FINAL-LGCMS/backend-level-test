package com.lgcms.leveltest.dto.request.leveltest;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class LevelTestRequest {

    @NotNull(message = "카테고리는 필수 입력 값입니다.")
    private Category category;

    @NotNull(message = "난이도는 필수 입력 값입니다.")
    private Difficulty difficulty;

    @NotBlank(message = "문제 내용은 필수 입력 값입니다.")
    private String question;

    @NotBlank(message = "모범답안은 필수 입력 값입니다.")
    private String answer;

    @NotNull(message = "필수 포함 키워드는 필수 입력 값입니다.")
    private List<String> mustInclude;
}