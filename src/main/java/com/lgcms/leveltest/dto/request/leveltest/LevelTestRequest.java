package com.lgcms.leveltest.dto.request.leveltest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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
    @JsonProperty("must_include")
    private List<String> mustInclude = new ArrayList<>();
}