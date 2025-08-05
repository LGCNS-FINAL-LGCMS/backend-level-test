package com.lgcms.leveltest.common.dto.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BaseException extends RuntimeException {
    private final ErrorCodeInterface errorCodeInterface;

    public ErrorCode getErrorCode() {
        return this.errorCodeInterface.getErrorCode();
    }
}