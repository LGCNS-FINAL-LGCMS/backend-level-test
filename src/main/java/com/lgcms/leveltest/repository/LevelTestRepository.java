package com.lgcms.leveltest.repository;

import com.lgcms.leveltest.domain.Category;
import com.lgcms.leveltest.domain.Difficulty;
import com.lgcms.leveltest.domain.LevelTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelTestRepository extends JpaRepository<LevelTest, Long> {

    List<LevelTest> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    List<LevelTest> findByCategory(Category category);

    List<LevelTest> findByDifficulty(Difficulty difficulty);

}
