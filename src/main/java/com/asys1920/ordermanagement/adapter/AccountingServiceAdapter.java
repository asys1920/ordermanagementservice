package com.asys1920.ordermanagement.adapter;

import com.asys1920.dto.BillDTO;
import com.asys1920.mapper.BillMapper;
import com.asys1920.model.Bill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountingServiceAdapter {
    @Value("${accounting.url}")
    private String accountingServiceUrl;
    final RestTemplate restTemplate;

    public AccountingServiceAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Saves a bill in the accounting service
     * @param bill the bill that gets saved in the service
     * @return the bill returned by the accounting service
     */
    public Bill saveBill(Bill bill) {
        BillDTO billDTO = BillMapper.INSTANCE.billToBillDTO(bill);
        HttpEntity<BillDTO> request = new HttpEntity<>(billDTO);
        return BillMapper.INSTANCE.billDTOtoBill(restTemplate.postForObject(accountingServiceUrl, request, BillDTO.class));
    }
}
