package com.asys1920.ordermanagement.service;

import com.asys1920.model.Bill;
import com.asys1920.model.Car;
import com.asys1920.model.Order;
import com.asys1920.model.User;
import com.asys1920.ordermanagement.adapter.AccountingServiceAdapter;
import com.asys1920.ordermanagement.adapter.CarServiceAdapter;
import com.asys1920.ordermanagement.adapter.UserServiceAdapter;
import com.asys1920.ordermanagement.exception.CarNotAvailableException;
import com.asys1920.ordermanagement.exception.OrderNotFoundException;
import com.asys1920.ordermanagement.exception.UserMayNotRentException;
import com.asys1920.ordermanagement.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
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

    public boolean userIsNotActive(Long userId) {
        LOG.trace(String.format("SERVICE %s %d initiated", "userIsNotActive", userId));
        User user = userServiceAdapter.getUser(userId);
        return !user.isActive();
    }

    public boolean userIsBanned(Long userId) {
        LOG.trace(String.format("SERVICE %s %d initiated", "userIsBanned", userId));
        return userServiceAdapter.getUser(userId).isBanned();
    }

    public boolean carIsEol(Long carId) {
        LOG.trace(String.format("SERVICE %s %d initiated", "carIsEol", carId));
        Car car = carServiceAdapter.getCar(carId);
        return car.isEol();
    }

    public boolean carIsInUse(Long carId, Instant time) {
        LOG.trace(String.format("SERVICE %s initiated", "carIsInUse"));
        // If list is empty, no order with this car has been made yet
        List<Order> allOrdersWithCar = orderRepository.findAllByCarId(carId);
        // If list is empty, the car is not available
        List<Order> validOrdersWithCar = orderRepository.findAllByCarIdAndEndDateNotNullAndEndDateIsBefore(carId, time);
        LOG.trace(String.format("SERVICE %s completed", "carIsInUse"));
        return !allOrdersWithCar.isEmpty() && validOrdersWithCar.isEmpty();
    }

    public Order createOrder(Order order) throws CarNotAvailableException, UserMayNotRentException {
        // Set start date on server to prevent fraud
        LOG.trace(String.format("SERVICE %s initiated", "createOrder"));
        if (carIsEol(order.getCarId())) {
            throw new CarNotAvailableException("The requested car is EOL");
        }
        if (carIsInUse(order.getCarId(), Instant.now())) {
            throw new CarNotAvailableException("The requested car is already in use");
        }
        if (userIsNotActive(order.getUserId()) || userIsBanned(order.getUserId())) {
            throw new UserMayNotRentException("The requested user is inactive or banned");
        }
        LOG.trace(String.format("SERVICE %s completed", "createOrder"));
        order.setStartDate(Instant.now());
        return orderRepository.save(order);
    }

    public Order reserveOrder(Order order) throws UserMayNotRentException, CarNotAvailableException {
        LOG.trace(String.format("SERVICE %s initiated", "reverseOrder"));
        if (userIsNotActive(order.getUserId()) || userIsBanned(order.getUserId())) {
            throw new UserMayNotRentException("The requested user is inactive or banned");
        }
        if (carIsEol(order.getCarId())) {
            throw new CarNotAvailableException("The requested car is EOL");
        }
        if (carIsInUse(order.getCarId(), order.getStartDate())) {
            throw new CarNotAvailableException("The requested car is already in use");
        }
        LOG.trace(String.format("SERVICE %s completed", "reverseOrder"));
        return orderRepository.save(order);
    }

    public Order finishOrder(Long orderId) throws OrderNotFoundException {
        LOG.trace(String.format("SERVICE %s %d initiated", "finishOrder", orderId));
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("The requested order was not found");
        }
        Order order = orderRepository.getOne(orderId);

        if (order.getStartDate().isAfter(Instant.now())) {
            order.setCanceled(true);
            order.setStartDate(null);
            order.setEndDate(null);
        } else {

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
            if (billableHours < 1) {
                billableHours = 1;
            }
            bill.setValue(carServiceAdapter.getCar(order.getCarId()).getCarBaseRentPrice() *
                    billableHours);

            bill = accountingServiceAdapter.saveBill(bill);
            order.setBillId(bill.getId());
        }
        LOG.trace(String.format("SERVICE %s %d completed", "finishOrder", orderId));
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        LOG.trace(String.format("SERVICE %s %d initiated", "getOrder", orderId));
        return orderRepository.getOne(orderId);
    }

    public List<Order> getAllOrders() {

        LOG.trace(String.format("SERVICE %s initiated", "getAllOrders"));
        return orderRepository.findAll();
    }

    public List<Order> getAllOrdersByUser(Long userId) {
        LOG.trace(String.format("SERVICE %s %d initiated", "getAllOrdersByUser", userId));
        return orderRepository.findAllByUserId(userId);
    }

    public List<Order> getAllOrdersByCar(Long carId) {
        LOG.trace(String.format("SERVICE %s %d initiated", "getAllOrdersByCar", carId));
        return orderRepository.findAllByCarId(carId);
    }
}
