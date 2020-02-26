package com.asys1920.ordermanagement.adapter;

import com.asys1920.dto.CarDTO;
import com.asys1920.mapper.CarMapper;
import com.asys1920.model.Car;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CarServiceAdapter {
    @Value("http://localhost:9091/cars/")
    private String carServiceUrl;
    final RestTemplate restTemplate;

    public CarServiceAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public Car getCar(Long carId) {
        CarDTO carDTO = restTemplate
                .getForObject(carServiceUrl + carId, CarDTO.class);
        return CarMapper.INSTANCE.carDTOToCar(carDTO);
    }
}
