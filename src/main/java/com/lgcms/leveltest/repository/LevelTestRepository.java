package com.lgcms.leveltest.repository;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import com.lgcms.leveltest.domain.LevelTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelTestRepository extends JpaRepository<LevelTest, Long> {

    // 카테고리와 난이도 기반 조회
    List<LevelTest> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    // 카테고리 기반 조회
    List<LevelTest> findByCategory(String category);

    // 난이도 기반 조회
    List<LevelTest> findByDifficulty(String difficulty);
}
