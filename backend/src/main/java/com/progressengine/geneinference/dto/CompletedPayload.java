package com.progressengine.geneinference.dto;

public class CompletedPayload {

    private String message;

    public CompletedPayload() {
    }

    public CompletedPayload(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
