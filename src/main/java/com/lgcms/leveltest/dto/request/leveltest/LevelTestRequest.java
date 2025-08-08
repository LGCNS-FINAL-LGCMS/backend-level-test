package com.lgcms.leveltest.dto.request.leveltest;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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
}