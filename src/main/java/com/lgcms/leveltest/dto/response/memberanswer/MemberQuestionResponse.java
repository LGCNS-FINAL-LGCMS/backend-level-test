package com.lgcms.leveltest.dto.response.memberanswer;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberQuestionResponse {
    private Long id;
    private Category category;
    private Difficulty difficulty;
    private String question;
}