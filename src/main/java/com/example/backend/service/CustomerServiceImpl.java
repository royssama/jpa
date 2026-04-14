package com.example.backend.service;

import com.example.backend.domain.Customer;
import com.example.backend.dto.CustomerRequest;
import com.example.backend.repository.jpa.CustomerJpaRepository;
import com.example.backend.repository.jpa.CustomerQueryRepository;
import com.example.backend.repository.mybatis.CustomerMyBatisMapper;
import com.example.backend.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JPA·QueryDSL·MyBatis 빈을 모두 주입합니다.
 * 메서드 안에서 호출만 바꿔서 원하는 스택으로 조회·변경하면 됩니다.
 */
@Primary
@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerQueryRepository customerQueryRepository;
    private final CustomerMyBatisMapper customerMyBatisMapper;

    public CustomerServiceImpl(
            CustomerJpaRepository customerJpaRepository,
            CustomerQueryRepository customerQueryRepository,
            CustomerMyBatisMapper customerMyBatisMapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.customerQueryRepository = customerQueryRepository;
        this.customerMyBatisMapper = customerMyBatisMapper;
    }

    /** 전체 조회 페이징 — JPA Pageable 사용. */
    @Override
    public Page<Customer> findAll(Pageable pageable) {
        return customerJpaRepository.findAll(pageable);
    }

    /** 단건 조회 — MyBatis로 바꾸려면 {@code customerMyBatisMapper.findById(id)} 사용. */
    @Override
    public Customer findById(Long id) {
       // return customerJpaRepository.findById(id).orElse(null);
        return customerMyBatisMapper.findById(id);
    }

    /** QueryDSL 검색 ({@link CustomerQueryRepository}). */
    @Override
    public List<Customer> searchByKeyword(String keyword) {
        return customerQueryRepository.findByKeywordContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional
    public Customer create(CustomerRequest request) {
        Customer customer = new Customer();
        applyRequest(customer, request);
        return customerJpaRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer update(Long id, CustomerRequest request) {
        Customer customer = customerJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        applyRequest(customer, request);
        return customer;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        customerJpaRepository.deleteById(id);
    }

    private static void applyRequest(Customer customer, CustomerRequest request) {
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setCompany(request.company());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setState(request.state());
        customer.setCountry(request.country());
        customer.setPostalCode(request.postalCode());
        customer.setPhone(request.phone());
        customer.setFax(request.fax());
        customer.setEmail(request.email());
        customer.setSupportRepId(request.supportRepId());
    }
}
