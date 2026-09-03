package com.shashireddy.logistics.controller;

import com.shashireddy.logistics.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Demo-only credential. A real deployment swaps this for a call to an
    // actual identity provider; JwtService and the filter chain stay the same.
    private static final String DEMO_USERNAME = "dispatcher";
    private static final String DEMO_PASSWORD = "demo-password";

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token) {
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (!DEMO_USERNAME.equals(request.username()) || !DEMO_PASSWORD.equals(request.password())) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(request.username())));
    }
}
