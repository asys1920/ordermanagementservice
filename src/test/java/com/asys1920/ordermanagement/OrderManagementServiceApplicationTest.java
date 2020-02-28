package com.asys1920.ordermanagement;


import com.asys1920.model.*;
import com.asys1920.ordermanagement.adapter.AccountingServiceAdapter;
import com.asys1920.ordermanagement.adapter.CarServiceAdapter;
import com.asys1920.ordermanagement.adapter.UserServiceAdapter;
import com.asys1920.ordermanagement.repository.OrderRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = OrderManagementApplication.class)
@AutoConfigureMockMvc
public class OrderManagementServiceApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceAdapter userServiceAdapter;

    @MockBean
    private CarServiceAdapter carServiceAdapter;

    @MockBean
    private AccountingServiceAdapter accountingServiceAdapter;

    @MockBean
    private OrderRepository repository;

    /*
    GET all orders
     */

    @Test
    public void should_return_OK_when_requesting_all_orders() throws Exception {
        mockMvc.perform(get("/orders")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void should_return_OK_when_requesting_all_orders_for_unknown_car() throws Exception {
        mockMvc.perform(get("/orders/bycar/" + 150)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void should_return_OK_when_requesting_all_orders_for_unknown_user() throws Exception {
        mockMvc.perform(get("/orders/byuser/" + 150)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /*
    Post order
    */

    @Test
    public void should_return_valid_order_when_creating_order() throws Exception {
        Order validOrder = getValidOrder();
        JSONObject body = jsonFromOrder(validOrder);

        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(createUser());
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());

        mockMvc.perform(post("/orders/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(body.get("userId")))
                .andExpect(jsonPath("$.carId").value(body.get("carId")));
    }

    @Test
    public void should_return_errorMessage_order_when_creating_order_user_inactive() throws Exception {
        Order validOrder = getValidOrder();
        JSONObject body =jsonFromOrder(validOrder);

        User user = createUser();
        user.setActive(false);
        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(user);
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());

        mockMvc.perform(post("/orders/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void should_return_errorMessage_order_when_creating_order_user_banned() throws Exception {
        Order validOrder = getValidOrder();
        JSONObject body =jsonFromOrder(validOrder);

        User user = createUser();
        user.setBanned(true);
        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(user);
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());

        mockMvc.perform(post("/orders/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void should_return_errorMessage_order_when_creating_order_car_eol() throws Exception {
        Order validOrder = getValidOrder();
        JSONObject body =jsonFromOrder(validOrder);

        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(createUser());
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Car car = createdCar();
        car.setEol(true);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(car);

        mockMvc.perform(post("/orders/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    // Reserve

    @Test
    public void should_return_valid_order_when_reserving_order() throws Exception {
        Order validOrder = getValidOrder();
        validOrder.setStartDate(Instant.now().plus(Duration.ofDays(2)));
        validOrder.setEndDate(validOrder.getStartDate().plus(Duration.ofDays(1)));
        JSONObject body = jsonFromOrder(validOrder);
        body.put("startDate", validOrder.getStartDate().toString());
        body.put("endDate", validOrder.getEndDate().toString());

        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(createUser());
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());

        mockMvc.perform(post("/orders/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(body.get("userId")))
                .andExpect(jsonPath("$.carId").value(body.get("carId")))
                .andExpect(jsonPath("$.startDate").value(body.get("startDate")))
                .andExpect(jsonPath("$.endDate").value(body.get("endDate")));
    }

    /*
    PATCH order
     */

    @Test
    public void should_return_errorMessage_when_patching_unknown_order() throws Exception {
        Order validOrder = getValidOrder();
        JSONObject body = jsonFromOrder(validOrder);

        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(createUser());
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());

        mockMvc.perform(patch("/orders/" + 1500)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void should_return_validOrder_when_patching_valid_order() throws Exception {
        Order validOrder = getValidOrder();
        validOrder.setStartDate(Instant.now().minus(Duration.ofDays(2)));
        JSONObject body = jsonFromOrder(validOrder);
        body.put("startDate", validOrder.getStartDate().toString());

        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(createUser());
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(repository.existsById(validOrder.getId())).thenReturn(true);
        Mockito.when(repository.getOne(validOrder.getId())).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());
        Mockito.when(accountingServiceAdapter.saveBill(Mockito.any(Bill.class))).thenReturn(getValidBill());

        mockMvc.perform(patch("/orders/" + validOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void should_return_validOrder_when_canceling_valid_order() throws Exception {
        Order validOrder = getValidOrder();
        validOrder.setStartDate(Instant.now().plus(Duration.ofDays(2)));
        validOrder.setEndDate(validOrder.getStartDate().plus(Duration.ofDays(1)));
        JSONObject body = jsonFromOrder(validOrder);
        body.put("startDate", validOrder.getStartDate().toString());
        body.put("endDate", validOrder.getEndDate().toString());

        Mockito.when(userServiceAdapter.getUser(validOrder.getUserId())).thenReturn(createUser());
        Mockito.when(repository.save(Mockito.any(Order.class))).thenReturn(validOrder);
        Mockito.when(repository.existsById(validOrder.getId())).thenReturn(true);
        Mockito.when(repository.getOne(validOrder.getId())).thenReturn(validOrder);
        Mockito.when(carServiceAdapter.getCar(validOrder.getCarId())).thenReturn(createdCar());
        Mockito.when(accountingServiceAdapter.saveBill(Mockito.any(Bill.class))).thenReturn(getValidBill());


        mockMvc.perform(patch("/orders/" + validOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(body.get("userId")))
                .andExpect(jsonPath("$.carId").value(body.get("carId")));
    }

    private long getRandomId() {
        return (long) (Math.random() * Integer.MAX_VALUE);
    }

    private JSONObject jsonFromOrder(Order order) throws JSONException {
        JSONObject jay = new JSONObject();
        jay.put("id", order.getId());
        jay.put("carId", order.getCarId());
        jay.put("userId", order.getUserId());
        return jay;
    }

    private Order getValidOrder() throws JSONException {
        Order order = new Order();
        User user = createUser();
        Car car = createdCar();
        order.setId(getRandomId());
        order.setUserId(user.getId());
        order.setCarId(car.getId());
        repository.save(order);
        return order;
    }

    private Bill getValidBill() throws JSONException {
        Bill bill = new Bill();
        User user = createUser();
        bill.setValue(200.0);
        bill.setId(1L);
        bill.setUserId(user.getId());
        bill.setCity(user.getCity());
        bill.setCountry(user.getCountry());
        bill.setName(user.getName());
        bill.setZipCode(user.getZipCode());
        bill.setCreationDate(Instant.now());
        bill.setPaymentDeadlineDate(Instant.now().plus(Period.ofDays(90)));
        return bill;
    }

    private JSONObject getValidUser() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("id", 1L);
        body.put("firstName", "Alexander");
        body.put("lastName", "Meier");
        body.put("userName", "Fussballgott");
        body.put("emailAddress", "a@b.c");
        body.put("expirationDateDriversLicense", Instant.now().toString());
        body.put("street", "Mörfelder Landstraße 362");
        body.put("zipCode", "60528");
        body.put("city", "Frankfurt am Main");
        body.put("country", "Germany");
        body.put("isActive", true);
        return body;
    }

    private User createUser() throws JSONException {
        long userId = 1L;
        User user = new User();
        JSONObject validUser = getValidUser();
        user.setId(userId);
        user.setFirstName(validUser.getString("firstName"));
        user.setLastName(validUser.getString("lastName"));
        user.setUserName(validUser.getString("userName"));
        user.setEmailAddress(validUser.getString("emailAddress"));
        user.setExpirationDateDriversLicense(Instant.now());
        user.setActive(validUser.getBoolean("isActive"));
        return user;
    }

    private JSONObject getValidCar() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("id", 1L);
        body.put("name", "TestCar");
        body.put("brand", "TestBrand");
        body.put("model", "TestModel");
        body.put("yearOfConstruction", 2010);
        body.put("numberOfDoors", 4);
        body.put("numberOfSeats", 4);
        body.put("carBaseRentPrice", 10.0);
        body.put("vehicleType", "SUV");
        return body;
    }

    private Car createdCar() throws JSONException {
        Car car = new Car();
        car.setId(1L);
        JSONObject validCar = getValidCar();
        car.setName(validCar.getString("name"));
        car.setBrand(validCar.getString("brand"));
        car.setModel(validCar.getString("model"));
        car.setYearOfConstruction(validCar.getInt("yearOfConstruction"));
        car.setNumberOfDoors(validCar.getInt("numberOfDoors"));
        car.setNumberOfSeats(validCar.getInt("numberOfSeats"));
        car.setCarBaseRentPrice(validCar.getDouble("carBaseRentPrice"));
        car.setVehicleType(VehicleType.get(validCar.getString("vehicleType")));
        return car;
    }
}
