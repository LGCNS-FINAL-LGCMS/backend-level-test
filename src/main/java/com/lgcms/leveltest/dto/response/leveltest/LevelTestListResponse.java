package com.lgcms.leveltest.dto.response.leveltest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelTestListResponse {
    private List<LevelTestResponse> questions;
}
