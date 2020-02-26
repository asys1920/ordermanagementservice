package com.asys1920.ordermanagement.controller;

import com.asys1920.dto.BillDTO;
import com.asys1920.dto.OrderDTO;
import com.asys1920.mapper.BillMapper;
import com.asys1920.mapper.OrderMapper;
import com.asys1920.model.Bill;
import com.asys1920.model.Order;
import com.asys1920.ordermanagement.exception.ValidationException;
import com.asys1920.ordermanagement.service.OrderService;
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

    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> finishOrder(@PathVariable long orderId) {
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(orderService.finishOrder(orderId)), HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable @NotNull long orderId) {
        return new ResponseEntity<>(OrderMapper.INSTANCE.orderToOrderDTO(orderService.getOrder(orderId)), HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        List<OrderDTO> allOrderDTOs = allOrders.stream()
                .map(OrderMapper.INSTANCE::orderToOrderDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(allOrderDTOs);
    }
}
