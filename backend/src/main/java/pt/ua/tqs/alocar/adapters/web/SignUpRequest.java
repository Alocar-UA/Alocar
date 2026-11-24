package pt.ua.tqs.alocar.adapters.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** US01: the token comes from the residence's join link; the email identifies the account. */
public record SignUpRequest(
        @NotBlank String joinToken,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email) {}
