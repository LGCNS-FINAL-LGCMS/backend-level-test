package com.lgcms.leveltest.common.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LevelTestError implements ErrorCodeInterface {

    INVALID_REQUEST("LETE-01", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("LETE-02", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QUESTION_NOT_FOUND("LETE-03", "해당 문제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CATEGORY("LETE-04", "유효하지 않은 카테고리입니다.", HttpStatus.BAD_REQUEST),
    INVALID_DIFFICULTY("LETE-05", "유효하지 않은 난이도입니다.", HttpStatus.BAD_REQUEST),
    ANSWER_NOT_FOUND("LETE-06", "해당 답변을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("LETE-07", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    GRADING_FAILED("LETE-08", "채점 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    GRADING_NOT_COMPLETED("LETE-11", "아직 모든 문제의 채점이 완료되지 않았습니다.", HttpStatus.BAD_REQUEST),
    REPORT_NOT_FOUND("LETE-12", "해당 레포트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String status;
    private final String message;
    private final HttpStatus httpStatus;

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.builder()
                .status(status)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}