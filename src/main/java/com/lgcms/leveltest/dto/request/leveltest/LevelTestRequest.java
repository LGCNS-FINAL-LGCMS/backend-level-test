package com.lgcms.leveltest.dto.request.leveltest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelTestRequest {

    @NotBlank(message = "카테고리는 필수 입력 값입니다.")
    @Size(max = 50)
    private String category;

    @NotBlank(message = "난이도는 필수 입력 값입니다.")
    @Size(max = 20)
    private String difficulty;

    @NotBlank(message = "문제 내용은 필수 입력 값입니다.")
    private String question;

    @NotBlank(message = "모범답안은 필수 입력 값입니다.")
    private String answer;
}