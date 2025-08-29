package com.lgcms.leveltest.dto.request.memberanswer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberAnswerRequest {

    @NotEmpty(message = "답변 목록은 비어있을 수 없습니다.")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "문제 ID는 필수입니다.")
        private Long questionId;

        @NotNull(message = "답변은 필수입니다.")
        private String answer;
    }
}