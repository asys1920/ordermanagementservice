package com.asys1920.ordermanagement.adapter;

import com.asys1920.dto.CarDTO;
import com.asys1920.mapper.CarMapper;
import com.asys1920.model.Car;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;

@Component
public class CarServiceAdapter {
    @Value("${car.url}")
    private String carServiceUrl;
    final RestTemplate restTemplate;

    public CarServiceAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    /**
     * Fetches a car from the car service
     *
     * @param carId the id to search for
     * @return the car from the service
     */
    public Car getCar(Long carId) throws ServiceUnavailableException {
        try {
            CarDTO carDTO = restTemplate
                    .getForObject(carServiceUrl + carId, CarDTO.class);
            return CarMapper.INSTANCE.carDTOToCar(carDTO);
        } catch (Exception ex) {
            throw new ServiceUnavailableException("CarService is currently unavailable. Please try again later.");
        }
    }
}
