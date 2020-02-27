package com.asys1920.ordermanagement.controller;

import com.asys1920.dto.OrderDTO;
import com.asys1920.mapper.OrderMapper;
import com.asys1920.model.Order;
import com.asys1920.ordermanagement.exception.ValidationException;
import com.asys1920.ordermanagement.service.OrderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @ApiOperation(value = "Create a new order", response = OrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created order"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) throws ValidationException {
        Set<ConstraintViolation<OrderDTO>> validate = Validation.buildDefaultValidatorFactory().getValidator().validate(orderDTO);
        if (!validate.isEmpty()) {
            throw new ValidationException(validate);
        }
        Order order = OrderMapper.INSTANCE.orderDTOToOrder(orderDTO);

        if(orderService.carIsEol(order.getCarId())) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(!orderService.userIsActive(order.getUserId()) || orderService.userIsBanned(order.getUserId())) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(orderService.carIsInUse(order.getCarId())) {
            return new ResponseEntity<>(HttpStatus.IM_USED);
        }
        // Update order by saving -> gets an ID assigned by the database
        order = orderService.createOrder(order);
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(order), HttpStatus.CREATED);
    }
    
    @ApiOperation(value = "Updates a specific Order", response = OrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated order"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> finishOrder(@PathVariable long orderId) {
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(orderService.finishOrder(orderId)), HttpStatus.OK);
    }
    
    @ApiOperation(value = "Get a existing order", response = OrderDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched order"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable @NotNull long orderId) {
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(orderService.getOrder(orderId)), HttpStatus.OK);
    }
    
    @ApiOperation(value = "Get all existing order", response = OrderDTO.class,  responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched orders"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")})
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        List<OrderDTO> allOrderDTOs = allOrders.stream()
                .map(OrderMapper.INSTANCE::orderToOrderDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(allOrderDTOs);
    }
}
