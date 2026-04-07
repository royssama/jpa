package com.example.backend.service;

import com.example.backend.domain.Customer;
import com.example.backend.dto.CustomerRequest;

import java.util.List;

public interface CustomerService {

    List<Customer> findAll();

    Customer findById(Long id);

    /** QueryDSL({@code CustomerQueryRepository}) 기반 검색. */
    List<Customer> searchByKeyword(String keyword);

    Customer create(CustomerRequest request);

    Customer update(Long id, CustomerRequest request);

    void delete(Long id);
}
