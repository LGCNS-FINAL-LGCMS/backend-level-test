package com.lgcms.leveltest.dto.response.leveltest;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class LevelTestResponse {
    private Long id;
    private Category category;
    private Difficulty difficulty;
    private String question;
    private String answer;
    private List<String> mustInclude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
