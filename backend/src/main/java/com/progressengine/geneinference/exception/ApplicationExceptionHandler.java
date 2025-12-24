package com.progressengine.geneinference.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String path = buildPath(ife);

            errors.put(
                    path,
                    "Invalid value '" + ife.getValue() +
                            "'. Expected type: " + ife.getTargetType().getSimpleName()
            );
        } else {
            errors.put("message", "Malformed JSON request");
        }

        return errors;
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

        return response;
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



}
