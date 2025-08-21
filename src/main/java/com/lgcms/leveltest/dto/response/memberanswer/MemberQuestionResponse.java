package com.lgcms.leveltest.dto.response.memberanswer;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberQuestionResponse {
    private final Long id;
    private final Category category;
    private final Difficulty difficulty;
    private final String question;
}