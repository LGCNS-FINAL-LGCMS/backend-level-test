package com.lgcms.leveltest.domain;

public enum Difficulty {
    HIGH("상"),
    MEDIUM("중"),
    LOW("하");

    private final String difficultyName;

    Difficulty(String difficultyName) {
        this.difficultyName = difficultyName;
    }

    public String getDifficultyName() {
        return difficultyName;
    }
}
