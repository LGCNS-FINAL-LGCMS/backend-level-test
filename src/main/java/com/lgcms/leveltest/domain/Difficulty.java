package com.lgcms.leveltest.domain;

import lombok.Getter;

@Getter
public enum Difficulty {
    HIGH("상"),
    MEDIUM("중"),
    LOW("하");

    private final String difficultyName;

    Difficulty(String difficultyName) {
        this.difficultyName = difficultyName;
    }
}
