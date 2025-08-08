package com.lgcms.leveltest.dto.response.memberanswer;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class MemberAnswerResponse {
    private Long id;
    private Long memberId;
    private Long questionId;
    private String questionContent;
    private String memberAnswer;
    private LocalDateTime createdAt;
}