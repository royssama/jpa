package com.example.backend.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface Test01Mapper {

    List<Map<String, Object>> findByRequest(Map<String, Object> req);
}
