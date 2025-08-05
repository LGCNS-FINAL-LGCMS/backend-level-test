package com.lgcms.leveltest.dto.response.leveltest;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class LevelTestResponse {
    private Long id;
    private String category;
    private String difficulty;
    private String question;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
