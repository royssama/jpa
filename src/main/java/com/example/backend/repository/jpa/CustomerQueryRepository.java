package com.example.backend.repository.jpa;

import com.example.backend.domain.Customer;

import java.util.List;

public interface CustomerQueryRepository {

    List<Customer> findByKeywordContainingIgnoreCase(String keyword);
}
