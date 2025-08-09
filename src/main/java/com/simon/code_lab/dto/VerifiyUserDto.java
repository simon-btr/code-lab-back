package com.simon.code_lab.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifiyUserDto {
    private String email;
    
    private String verificationCode;
}
