package com.lgcms.leveltest.domain;

import lombok.Getter;

@Getter
public enum StudentLevel {
    HIGH("상"),
    MEDIUM("중"),
    LOW("하");

    private final String levelName;

    StudentLevel(String levelName) {this.levelName = levelName;}
}