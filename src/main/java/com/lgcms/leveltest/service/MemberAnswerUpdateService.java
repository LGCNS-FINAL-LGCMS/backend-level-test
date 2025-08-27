package com.lgcms.leveltest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcms.leveltest.domain.MemberAnswer;
import com.lgcms.leveltest.dto.response.scoring.ScoringResult;
import com.lgcms.leveltest.repository.MemberAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAnswerUpdateService {

    private final MemberAnswerRepository memberAnswerRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void updateWithScoringResult(MemberAnswer memberAnswer, ScoringResult result) {
        String mustIncludeMatchedStr = null;
        if (result.getMustIncludeMatched() != null) {
            mustIncludeMatchedStr = String.join(",", result.getMustIncludeMatched());
        }

        String detailsJson = null;
        String conceptAnalysesJson = null;
        try {
            if (result.getScoringDetails() != null) {
                detailsJson = objectMapper.writeValueAsString(result.getScoringDetails());
            }
            if (result.getConceptAnalyses() != null) {
                conceptAnalysesJson = objectMapper.writeValueAsString(result.getConceptAnalyses());
            }
        } catch (JsonProcessingException e) {
            log.error("점수 데이터 직렬화 실패", e);
        }

        MemberAnswer updatedAnswer = MemberAnswer.builder()
                .id(memberAnswer.getId())
                .memberId(memberAnswer.getMemberId())
                .question(memberAnswer.getQuestion())
                .memberAnswer(memberAnswer.getMemberAnswer())
                .score(result.getScore())
                .feedback(result.getFeedback())
                .isScored(true)
                .scoringDetails(detailsJson)
                .conceptAnalyses(conceptAnalysesJson)
                .mustIncludeMatched(mustIncludeMatchedStr)
                .scoredAt(LocalDateTime.now())
                .scoringModel("anthropic.claude-3-haiku-20240307-v1:0")
                .createdAt(memberAnswer.getCreatedAt())
                .build();

        memberAnswerRepository.save(updatedAnswer);
    }
}