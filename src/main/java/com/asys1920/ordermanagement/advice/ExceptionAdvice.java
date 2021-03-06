package com.asys1920.ordermanagement.advice;

import com.asys1920.ordermanagement.exception.CarNotAvailableException;
import com.asys1920.ordermanagement.exception.IllegalReservationException;
import com.asys1920.ordermanagement.exception.OrderNotFoundException;
import com.asys1920.ordermanagement.exception.UserMayNotRentException;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.ServiceUnavailableException;
import javax.persistence.EntityNotFoundException;

@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(value = {CarNotAvailableException.class, UserMayNotRentException.class})
    @ResponseBody
    public ResponseEntity<JSONObject> handleEntityPermissionDenied(Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {IllegalReservationException.class})
    @ResponseBody
    public ResponseEntity<JSONObject> handleIllegalReservation(Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class, OrderNotFoundException.class})
    @ResponseBody
    public ResponseEntity<JSONObject> handleEntityNotFound(Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseBody
    public ResponseEntity<JSONObject> handleAdapterNotConnected(Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return new ResponseEntity<>(jsonFromException(ex), HttpStatus.FAILED_DEPENDENCY);
    }

    private JSONObject jsonFromException(Exception ex) {
        JSONObject response = new JSONObject();
        response.put("message", ex.getMessage());
        return response;
    }
}