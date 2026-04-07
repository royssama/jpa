package com.example.backend.dto;

import com.example.backend.domain.Customer;

public record CustomerResponse(
        Long customerId,
        String firstName,
        String lastName,
        String company,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        String phone,
        String fax,
        String email,
        Integer supportRepId
) {

    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(
                c.getCustomerId(),
                c.getFirstName(),
                c.getLastName(),
                c.getCompany(),
                c.getAddress(),
                c.getCity(),
                c.getState(),
                c.getCountry(),
                c.getPostalCode(),
                c.getPhone(),
                c.getFax(),
                c.getEmail(),
                c.getSupportRepId()
        );
    }
}
