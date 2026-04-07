package com.example.backend.repository.mybatis;

import com.example.backend.domain.Customer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomerMyBatisMapper {

    List<Customer> findAll();

    Customer findById(Long id);

    int insert(Customer customer);

    int update(Customer customer);

    int deleteById(Long id);
}
