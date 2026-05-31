package com.utn.magtea.auth;

public record LoginResponse(String token, String email, String role) {}
