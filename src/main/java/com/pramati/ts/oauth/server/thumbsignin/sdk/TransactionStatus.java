package com.pramati.ts.oauth.server.thumbsignin.sdk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionStatus {

    COMPLETED_SUCCESSFUL("COMPLETED_SUCCESSFUL"),

    COMPLETED_FAILURE("COMPLETED_FAILURE"),

    DECLINED("DECLINED"),

    CANCELLED("CANCELLED"),

    TIMEOUT("TIMEOUT"),

    PENDING("PENDING"),

    INITIATED("INITIATED");

    private String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public TransactionStatus fromValue(String text) {
        for (TransactionStatus b : TransactionStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
