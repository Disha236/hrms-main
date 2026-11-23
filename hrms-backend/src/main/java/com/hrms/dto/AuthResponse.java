package com.hrms.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UserSessionDto user;

    public AuthResponse(String token, UserSessionDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserSessionDto getUser() { return user; }
    public void setUser(UserSessionDto user) { this.user = user; }
}
