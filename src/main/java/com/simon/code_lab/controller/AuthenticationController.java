package com.simon.code_lab.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simon.code_lab.dto.EmailDto;
import com.simon.code_lab.dto.LoginUserDto;
import com.simon.code_lab.dto.RegisterUserDto;
import com.simon.code_lab.dto.VerifiyUserDto;
import com.simon.code_lab.exception.UserNotEnabledException;
import com.simon.code_lab.model.User;
import com.simon.code_lab.response.LoginResponse;
import com.simon.code_lab.service.AuthenticationService;
import com.simon.code_lab.service.JwtService;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User user = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User user = authenticationService.authenticate(loginUserDto);
            String token = jwtService.generateToken(user);

            LoginResponse loginResponse = new LoginResponse(token, jwtService.getExpiration());
            return ResponseEntity.ok(loginResponse);
        } catch (UserNotEnabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifiyUserDto verifiyUserDto) {
        try {
            authenticationService.verifyUser(verifiyUserDto);
            return ResponseEntity.ok("Account verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody EmailDto emailDto) {
        try {
            authenticationService.resendVerificationCode(emailDto.getEmail());
            return ResponseEntity.ok("Verification email resent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
