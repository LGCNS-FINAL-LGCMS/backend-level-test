package com.lgcms.leveltest.service;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.CategoryItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryMappingService {

    // Redis 카테고리명 - DB Category enum 매핑
    private static final Map<String, Category> CATEGORY_MAPPING = Map.of(
            "알고리즘", Category.ALGORITHM,
            "SQL", Category.DATABASE,
            "docker", Category.DOCKER,
            "Git", Category.GIT,
            "자바", Category.JAVA,
            "Node.js", Category.NODEJS,
            "파이썬", Category.PYTHON,
            "리액트", Category.REACT,
            "스프링", Category.SPRING,
            "Vue", Category.VUEJS
    );

    public List<Category> mapToDbCategories(List<CategoryItem> memberCategories) {
        return memberCategories.stream()
                .map(categoryItem -> CATEGORY_MAPPING.get(categoryItem.name()))
                .filter(category -> category != null)
                .distinct()
                .toList();
    }
}