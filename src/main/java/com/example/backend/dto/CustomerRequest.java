package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String company,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        String phone,
        String fax,
        @NotBlank @Email String email,
        Integer supportRepId
) {
}
