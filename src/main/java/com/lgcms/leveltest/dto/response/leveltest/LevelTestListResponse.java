package com.lgcms.leveltest.dto.response.leveltest;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class LevelTestListResponse {
    private List<LevelTestResponse> questions;
}
