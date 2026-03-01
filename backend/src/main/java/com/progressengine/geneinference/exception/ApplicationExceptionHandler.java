package com.progressengine.geneinference.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.progressengine.geneinference.dto.ErrorResponse;
import com.progressengine.geneinference.model.ExcessAlleleViolation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
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
    public Map<String, Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Incomplete or invalid request");

        Map<String, Object> errorsOut = new LinkedHashMap<>();

        Map<String, List<String>> genotypesByCategory = new LinkedHashMap<>();
        Map<String, List<String>> distributionsByCategory = new LinkedHashMap<>();
        Map<String, List<String>> otherErrors = new LinkedHashMap<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String path = fe.getField();          // e.g. "genotypes[SWIM]" or "distributions[SWIM]"
            String msg  = fe.getDefaultMessage();

            // --- genotypes[CAT] ---
            int gIdx = path.indexOf("genotypes[");
            if (gIdx >= 0) {
                String category = extractBracketKey(path, gIdx + "genotypes[".length());
                if (category != null) {
                    genotypesByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(msg);
                    continue;
                }
            }

            // --- distributions[CAT] ---
            int dIdx = path.indexOf("distributions[");
            if (dIdx >= 0) {
                String category = extractBracketKey(path, dIdx + "distributions[".length());
                if (category != null) {
                    distributionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(msg);
                    continue;
                }
            }

            otherErrors.computeIfAbsent(path, k -> new ArrayList<>()).add(msg);
        }

        if (!otherErrors.isEmpty()) errorsOut.putAll(otherErrors);
        if (!genotypesByCategory.isEmpty()) errorsOut.put("genotypes", genotypesByCategory);
        if (!distributionsByCategory.isEmpty()) errorsOut.put("distributions", distributionsByCategory);

        response.put("errors", errorsOut);
        return response;
    }

    private static String extractBracketKey(String s, int start) {
        int end = s.indexOf(']', start);
        if (end <= start) return null;
        return s.substring(start, end);
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
        return Map.of("message", ex.getMessage(), "trace", Arrays.toString(ex.getStackTrace()));
    }

    @ExceptionHandler(IncompleteGenotypeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIncompleteGenotype(IncompleteGenotypeException ex) {
        return Map.of(
                "message", ex.getMessage(),
                "errors", Map.of(
                        "parent1MissingCategories", ex.getParent1Missing(),
                        "parent2MissingCategories", ex.getParent2Missing()
                )
        );
    }

    @ExceptionHandler(ExcessAlleleDiversityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleExcessAlleleDiversity(
            ExcessAlleleDiversityException ex
    ) {

        Map<String, Object> categoryErrors = new LinkedHashMap<>();

        for (ExcessAlleleViolation v : ex.getViolations()) {
            categoryErrors.put(
                    v.category().name(),
                    Map.of(
                            "attemptedAllele", v.attemptedAllele(),
                            "validAlleles", v.validAlleles()
                    )
            );
        }

        return ErrorResponse.builder()
                .error("GENETIC_CONSTRAINT_VIOLATION")
                .message(ex.getMessage())
                .errors(Map.of("genotypes", categoryErrors))
                .suggestions(List.of("Choose one of the valid alleles listed.", "If you believe an allele is correct, review earlier birth records to confirm phenotypes at birth."))
                .build();
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
