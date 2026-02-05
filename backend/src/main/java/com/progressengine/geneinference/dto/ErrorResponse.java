package com.progressengine.geneinference.dto;

import java.util.Map;

public class ErrorResponse {

    private String error;
    private String message;
    private Map<String, Object> details;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ErrorResponse response = new ErrorResponse();

        public Builder error(String error) {
            response.error = error;
            return this;
        }

        public Builder message(String message) {
            response.message = message;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            response.details = details;
            return this;
        }

        public ErrorResponse build() {
            return response;
        }
    }
}
