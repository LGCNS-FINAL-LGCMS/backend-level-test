package com.lgcms.leveltest.common.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryError implements ErrorCodeInterface {
    NO_SUCH_CATEGORY("CATEGORY_001", "해당 카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
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