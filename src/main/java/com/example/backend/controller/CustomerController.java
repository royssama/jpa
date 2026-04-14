package com.example.backend.controller;

import com.example.backend.api.CommonApiResponse;
import com.example.backend.api.CommonApiResponseFactory;
import com.example.backend.domain.Customer;
import com.example.backend.dto.CustomerRequest;
import com.example.backend.dto.CustomerResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@Tag(name = "Customer")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CommonApiResponseFactory responseFactory;

    public CustomerController(CustomerService customerService, CommonApiResponseFactory responseFactory) {
        this.customerService = customerService;
        this.responseFactory = responseFactory;
    }

    @Operation(summary = "전체 조회")
    @GetMapping
    public CommonApiResponse<Page<CustomerResponse>> list(Pageable pageable, Locale locale) {
        return responseFactory.success(
                customerService.findAll(pageable).map(CustomerResponse::from),locale == null ? Locale.getDefault() : locale);
    }

    @Operation(summary = "단건 조회")
    @GetMapping("/{id}")
    public CommonApiResponse<CustomerResponse> get(@PathVariable Long id, Locale locale) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        return responseFactory.success(CustomerResponse.from(customer), locale);
    }

    @Operation(summary = "이름·이메일 검색 (JPA·QueryDSL 사용 시)")
    @GetMapping("/search")
    public CommonApiResponse<List<CustomerResponse>> search(@RequestParam String keyword, Locale locale) {
        return responseFactory.success(
                customerService.searchByKeyword(keyword).stream().map(CustomerResponse::from).toList(),
                locale
        );
    }

    @Operation(summary = "등록")
    @PostMapping
    public CommonApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest request, Locale locale) {
        Customer saved = customerService.create(request);
        return responseFactory.success("response.customer.created", CustomerResponse.from(saved), locale);
    }

    @Operation(summary = "수정")
    @PutMapping("/{id}")
    public CommonApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request, Locale locale) {
        Customer updated = customerService.update(id, request);
        return responseFactory.success(CustomerResponse.from(updated), locale);
    }

    @Operation(summary = "삭제")
    @DeleteMapping("/{id}")
    public CommonApiResponse<Void> delete(@PathVariable Long id, Locale locale) {
        customerService.delete(id);
        return responseFactory.success("response.customer.deleted", null, locale);
    }
}
