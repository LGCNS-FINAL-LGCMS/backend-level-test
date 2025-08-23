package com.lgcms.leveltest.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record CategoryItem(
        Long id,
        String name
) {
}