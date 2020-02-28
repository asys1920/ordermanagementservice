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
    @Value("http://localhost:${adapter.accounting.port}/bills/")
    private String accountingServiceUrl;
    final RestTemplate restTemplate;

    public AccountingServiceAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public Bill saveBill(Bill bill) {
        BillDTO billDTO = BillMapper.INSTANCE.billToBillDTO(bill);
        HttpEntity<BillDTO> request = new HttpEntity<>(billDTO);
        return BillMapper.INSTANCE.billDTOtoBill(restTemplate.postForObject(accountingServiceUrl, request, BillDTO.class));
    }
}
