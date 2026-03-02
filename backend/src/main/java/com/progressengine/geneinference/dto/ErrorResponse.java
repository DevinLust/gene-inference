package com.progressengine.geneinference.dto;

import java.util.List;
import java.util.Map;

public class ErrorResponse {

    private String error;
    private String message;
    private Map<String, Object> errors;
    private List<String> suggestions;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    public List<String> getSuggestions() {
        return suggestions;
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

        public Builder errors(Map<String, Object> errors) {
            response.errors = errors;
            return this;
        }

        public Builder suggestions(List<String> suggestions) {
            response.suggestions = suggestions;
            return this;
        }

        public ErrorResponse build() {
            return response;
        }
    }
}
