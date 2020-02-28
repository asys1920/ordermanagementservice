package com.asys1920.ordermanagement.advice;

import com.asys1920.ordermanagement.exception.CarNotAvailableException;
import com.asys1920.ordermanagement.exception.IllegalReservationException;
import com.asys1920.ordermanagement.exception.OrderNotFoundException;
import com.asys1920.ordermanagement.exception.UserMayNotRentException;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;

@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {CarNotAvailableException.class, UserMayNotRentException.class})
    @ResponseBody
    public ResponseEntity<String> handleEntityPermissionDenied(Exception ex) {
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {IllegalReservationException.class})
    @ResponseBody
    public ResponseEntity<String> handleIllegalReservation(Exception ex) {
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class, OrderNotFoundException.class})
    @ResponseBody
    public ResponseEntity<String> handleEntityNotFound(Exception ex) {
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.NOT_FOUND);
    }

    private String jsonFromException(Exception ex) {
        JSONObject response = new JSONObject();
        response.put("message", ex.getMessage());
        return response.toJSONString();
    }
}