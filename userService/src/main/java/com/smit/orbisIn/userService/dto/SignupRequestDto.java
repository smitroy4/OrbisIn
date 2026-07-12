package com.smit.orbisIn.userService.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String name, email, password;
}
