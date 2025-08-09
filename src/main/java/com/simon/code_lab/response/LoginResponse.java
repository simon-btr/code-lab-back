package com.simon.code_lab.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;

    private long expireIn;

    public LoginResponse(String token, long expireIn) {
        this.token = token;
        this.expireIn = expireIn;
    }
}
