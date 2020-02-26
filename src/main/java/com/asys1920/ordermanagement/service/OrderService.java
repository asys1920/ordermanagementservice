package com.asys1920.ordermanagement.service;

import com.asys1920.model.Bill;
import com.asys1920.model.Order;
import com.asys1920.model.User;
import com.asys1920.ordermanagement.adapter.AccountingServiceAdapter;
import com.asys1920.ordermanagement.adapter.CarServiceAdapter;
import com.asys1920.ordermanagement.adapter.UserServiceAdapter;
import com.asys1920.ordermanagement.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final UserServiceAdapter userServiceAdapter;
    private final CarServiceAdapter carServiceAdapter;
    private final AccountingServiceAdapter accountingServiceAdapter;

    public OrderService(OrderRepository orderRepository, UserServiceAdapter userServiceAdapter, CarServiceAdapter carServiceAdapter, AccountingServiceAdapter accountingServiceAdapter) {
        this.orderRepository = orderRepository;
        this.userServiceAdapter = userServiceAdapter;
        this.carServiceAdapter = carServiceAdapter;
        this.accountingServiceAdapter = accountingServiceAdapter;
    }

    public boolean userIsActive(Long userId) {
        return userServiceAdapter.getUser(userId).isActive();
    }

    public boolean userIsBanned(Long userId) {
        return userServiceAdapter.getUser(userId).isBanned();
    }

    public boolean carIsEol(Long carId) {
        return carServiceAdapter.getCar(carId).isEol();
    }

    public boolean carIsInUse(Long carId) {
        // If list is empty, no order with this car has been made yet
        List<Order> allOrdersWithCar = orderRepository.findAllByCarId(carId);
        // If list is empty, the car is not available
        List<Order> validOrdersWithCar = orderRepository.findAllByCarIdAndEndDateNotNullAndEndDateIsBefore(carId, Instant.now());
        return !allOrdersWithCar.isEmpty() && validOrdersWithCar.isEmpty();
    }

    public Order createOrder(Order order) {
        // Set start date on server to prevent fraud
        order.setStartDate(Instant.now());
        return orderRepository.save(order);
    }

    public Order finishOrder(Long orderId) {
        Order order = orderRepository.getOne(orderId);

        // Set end date on server to prevent fraud
        order.setEndDate(Instant.now());
        Bill bill = new Bill();
        bill.setUserId(order.getUserId());
        User user = userServiceAdapter.getUser(order.getUserId());
        bill.setCity(user.getCity());
        bill.setCountry(user.getCountry());
        bill.setName(user.getName());
        bill.setStreet(user.getStreet());
        bill.setZipCode(user.getZipCode());
        // Bill created right now
        bill.setCreationDate(Instant.now());
        // Payment is due in 7 days
        bill.setPaymentDeadlineDate(bill.getCreationDate().plus(Duration.ofDays(7)));

        long billableHours = Duration.between(order.getStartDate(), order.getEndDate()).toHours();
        if(billableHours < 1) {
            billableHours = 1;
        }
        //TODO actual calculation with pricecalculationservice

        bill.setValue(carServiceAdapter.getCar(order.getCarId()).getCarBaseRentPrice() *
                billableHours);

        bill = accountingServiceAdapter.saveBill(bill);
        order.setBillId(bill.getId());
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.getOne(orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
