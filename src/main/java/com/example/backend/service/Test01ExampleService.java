package com.example.backend.service;

import com.example.backend.repository.mybatis.Test01Mapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Test01ExampleService {

    private final Test01Mapper test01Mapper;

    public Test01ExampleService(Test01Mapper test01Mapper) {
        this.test01Mapper = test01Mapper;
    }

    public List<Map<String, Object>> findExample() {
        Map<String, Object> req = new HashMap<>();
        req.put("AUTH_ID", 1234);
        req.put("USER_ID", "");

        return test01Mapper.findByRequest(req);
    }
}
