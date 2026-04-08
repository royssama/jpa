package com.example.backend.service;

import com.example.backend.domain.Customer;
import com.example.backend.dto.CustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    Page<Customer> findAll(Pageable pageable);

    Customer findById(Long id);

    /** QueryDSL({@code CustomerQueryRepository}) 기반 검색. */
    List<Customer> searchByKeyword(String keyword);

    Customer create(CustomerRequest request);

    Customer update(Long id, CustomerRequest request);

    void delete(Long id);
}
