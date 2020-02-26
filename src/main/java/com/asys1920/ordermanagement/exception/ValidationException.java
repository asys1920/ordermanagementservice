package com.asys1920.ordermanagement.exception;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationException extends Exception {
    public <T> ValidationException(Set<ConstraintViolation<T>> violations) {
        super(violations.stream().map(
                ConstraintViolation::getMessage)
                .collect(Collectors.joining(System.lineSeparator())));
    }

}
