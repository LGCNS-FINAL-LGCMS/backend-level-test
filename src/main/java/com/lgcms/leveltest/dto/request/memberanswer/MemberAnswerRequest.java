package com.lgcms.leveltest.dto.request.memberanswer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberAnswerRequest {

    @NotNull(message = "문제 ID는 필수 입력 값입니다.")
    private Long questionId;

    @NotBlank(message = "답변은 필수 입력 값입니다.")
    private String answer;
}