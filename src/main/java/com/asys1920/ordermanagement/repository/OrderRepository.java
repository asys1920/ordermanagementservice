package com.asys1920.ordermanagement.repository;

import com.asys1920.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCarIdAndEndDateNotNullAndEndDateIsBefore(Long carId, Instant now);
    List<Order> findAllByCarId(Long carId);
    List<Order> findAllByUserId(Long userId);
}
