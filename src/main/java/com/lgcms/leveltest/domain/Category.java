package com.lgcms.leveltest.domain;

public enum Category {
    ALGORITHM("Algorithm"),
    DATABASE("Database"),
    DOCKER("Docker"),
    GIT("Git"),
    JAVA("Java"),
    NODEJS("Node.js"),
    PYTHON("Python"),
    REACT("React"),
    SPRING("Spring"),
    VUEJS("Vue.js");

    private final String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

}
