package com.project.heritagelink.exception;

/**
 * Thrown when a requested operation violates a domain business rule,
 * e.g. attempting a SALE disposition without a verified appraisal value.
 */
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
