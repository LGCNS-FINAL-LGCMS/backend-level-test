package com.lgcms.leveltest.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record MemberCategory(
        Long id,
        String name
) {
}