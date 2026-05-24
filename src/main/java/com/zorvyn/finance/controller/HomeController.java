package com.zorvyn.finance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "running");
        response.put("application", "Finance Dashboard API");
        response.put("version", "1.0.0");
        response.put("message", "Welcome to Finance Dashboard API");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Register", "POST /api/auth/register");
        endpoints.put("Login", "POST /api/auth/login");
        endpoints.put("Dashboard", "GET /api/dashboard");
        
        response.put("available_endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }
}