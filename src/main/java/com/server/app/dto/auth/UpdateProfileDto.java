package com.server.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDto {
    @Size(min = 3, max = 20)
    private String username;

    @Size(min = 2, max = 50)
    private String name;

    @Size(min = 2, max = 50)
    private String surname;

    @Email
    private String email;
}