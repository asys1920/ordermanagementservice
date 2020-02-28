package com.asys1920.ordermanagement.controller;

import com.asys1920.dto.OrderDTO;
import com.asys1920.mapper.OrderMapper;
import com.asys1920.model.Order;
import com.asys1920.ordermanagement.exception.*;
import com.asys1920.ordermanagement.service.OrderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class OrderController {
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
    private static final String PATH = "/orders";
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @ApiOperation(value = "Create a new order or reservation", response = OrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created order"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @PostMapping(PATH)
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) throws ValidationException, CarNotAvailableException, UserMayNotRentException, IllegalReservationException {
        LOG.trace(String.format("POST %s initiated", PATH));
        Set<ConstraintViolation<OrderDTO>> validate = Validation.buildDefaultValidatorFactory().getValidator().validate(orderDTO);
        if (!validate.isEmpty()) {
            throw new ValidationException(validate);
        }
        Order order = OrderMapper.INSTANCE.orderDTOToOrder(orderDTO);

        if(order.getStartDate() != null && order.getStartDate().isAfter(Instant.now())) {
            // If startDate already exists and is in future, it's a reserve request
            if(order.getEndDate() != null && order.getEndDate().isAfter(order.getStartDate())) {
                // If order has an endDate after startDate
                order = orderService.reserveOrder(order);
            }
            else {
                throw new IllegalReservationException("Requested reservation has no end date");
            }
        }
        else {
            // If startDate does not exist, it's a normal order starting exactly now
            order = orderService.createOrder(order);
        }
        LOG.trace(String.format("POST %s completed", PATH));
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(order), HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Updates a specific Order", response = OrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated order"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @PatchMapping(PATH+"/{orderId}")
    public ResponseEntity<OrderDTO> finishOrder(@PathVariable long orderId) throws OrderNotFoundException {
        LOG.trace(String.format("PATCH %s initiated", PATH));
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(orderService.finishOrder(orderId)), HttpStatus.OK);
    }
    
    @ApiOperation(value = "Get a existing order", response = OrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched order"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @GetMapping(PATH+"/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable @NotNull long orderId) {
        LOG.trace(String.format("GET %s initiated", PATH));
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(orderService.getOrder(orderId)), HttpStatus.OK);
    }
    
    @ApiOperation(value = "Get all existing order", response = OrderDTO.class,  responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched orders"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @GetMapping(PATH)
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        LOG.trace(String.format("GET %s initiated", PATH));
        List<Order> allOrders = orderService.getAllOrders();
        List<OrderDTO> allOrderDTOs = allOrders.stream()
                .map(OrderMapper.INSTANCE::orderToOrderDTO)
                .collect(Collectors.toList());
        LOG.trace(String.format("GET %s completed", PATH));
        return ResponseEntity.ok().body(allOrderDTOs);
    }

    @GetMapping(PATH+"/bycar/{carId}")
    public ResponseEntity<List<OrderDTO>> getAllOrdersByCar(@PathVariable long carId) {
        LOG.trace(String.format("GET %s%s initiated", PATH,"/bycar"));
        List<Order> allOrders = orderService.getAllOrdersByCar(carId);
        List<OrderDTO> allOrderDTOs = allOrders.stream()
                .map(OrderMapper.INSTANCE::orderToOrderDTO)
                .collect(Collectors.toList());
        LOG.trace(String.format("GET %s%s completed", PATH,"/bycar"));
        return ResponseEntity.ok().body(allOrderDTOs);
    }

    @GetMapping(PATH+"/byuser/{userId}")
    public ResponseEntity<List<OrderDTO>> getAllOrdersByUser(@PathVariable long userId) {
        LOG.trace(String.format("GET %s%s initiated", PATH,"/byuser"));
        List<Order> allOrders = orderService.getAllOrdersByUser(userId);
        List<OrderDTO> allOrderDTOs = allOrders.stream()
                .map(OrderMapper.INSTANCE::orderToOrderDTO)
                .collect(Collectors.toList());
        LOG.trace(String.format("GET %s%s completed", PATH,"/byuser"));
        return ResponseEntity.ok().body(allOrderDTOs);
    }
}
