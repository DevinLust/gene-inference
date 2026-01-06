package com.progressengine.geneinference.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            List<String> path = extractPath(ife);

            putNestedError(
                    errors,
                    path,
                    "Invalid value '" + ife.getValue() +
                            "'. Expected type: " + ife.getTargetType().getSimpleName()
            );

            response.put("message", "Invalid request payload");
        } else {
            response.put("message", "Malformed JSON request");
        }

        response.put("errors", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return response;
    }




    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgumentException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                        .add(error.getDefaultMessage())
        );

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.computeIfAbsent(error.getObjectName(), k -> new ArrayList<>())
                        .add(error.getDefaultMessage())
        );

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        response.put("message", "Incomplete or invalid request");

        return response;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            Iterator<Path.Node> it = v.getPropertyPath().iterator();
                            Path.Node last = null;
                            while (it.hasNext()) {
                                last = it.next();
                            }
                            return last != null ? last.getName() : "parameter";
                        },
                        ConstraintViolation::getMessage
                ));
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", fieldErrors);
        response.put("message", "Validation failed");

        return response;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // or BAD_REQUEST
    public Map<String, String> handleIllegalState(IllegalStateException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(IncompleteGenotypeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIncompleteGenotype(IncompleteGenotypeException ex) {
        return Map.of(
                "message", ex.getMessage(),
                "details", Map.of(
                        "parent1MissingCategories", ex.getParent1Missing(),
                        "parent2MissingCategories", ex.getParent2Missing()
                )
        );
    }

    private String buildPath(InvalidFormatException ife) {
        StringBuilder path = new StringBuilder();

        for (var ref : ife.getPath()) {
            if (ref.getFieldName() != null) {
                if (!path.isEmpty()) path.append(".");
                path.append(ref.getFieldName());
            } else if (ref.getDescription() != null) {
                if (!path.isEmpty()) path.append(".");
                path.append(ref.getDescription());
            } else if (ref.getIndex() >= 0) {
                path.append("[").append(ref.getIndex()).append("]");
            }
        }

        return path.isEmpty() ? "unknown" : path.toString();
    }

    private List<String> extractPath(InvalidFormatException ex) {
        return ex.getPath().stream()
                .map(ref -> ref.getFieldName() != null
                        ? ref.getFieldName()
                        : String.valueOf(ref.getIndex()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private void putNestedError(
            Map<String, Object> root,
            List<String> path,
            String message
    ) {
        Map<String, Object> current = root;

        for (int i = 0; i < path.size(); i++) {
            String key = path.get(i);

            if (i == path.size() - 1) {
                current
                        .computeIfAbsent(key, k -> new ArrayList<String>());
                ((List<String>) current.get(key)).add(message);
            } else {
                current = (Map<String, Object>)
                        current.computeIfAbsent(key, k -> new HashMap<>());
            }
        }
    }


}
