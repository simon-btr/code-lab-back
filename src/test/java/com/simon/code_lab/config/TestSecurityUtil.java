package com.simon.code_lab.config;

public class TestSecurityUtil {

    private static String authenticatedEmail;

    public static void setAuthenticatedEmail(String email) {
        authenticatedEmail = email;
    }

    public static String getAuthenticatedEmail() {
        return authenticatedEmail;
    }
}
