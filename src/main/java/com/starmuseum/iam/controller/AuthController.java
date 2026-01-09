package com.starmuseum.iam.controller;

import com.starmuseum.common.api.Result;
import com.starmuseum.iam.dto.LoginRequest;
import com.starmuseum.iam.dto.LogoutRequest;
import com.starmuseum.iam.dto.RefreshRequest;
import com.starmuseum.iam.dto.RegisterRequest;
import com.starmuseum.iam.service.AuthService;
import com.starmuseum.iam.vo.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器：
 * - register / login / refresh 放行（在 SecurityConfig 里 permitAll）
 * - logout 需要登录（或你也可以允许不登录，只要给 refreshToken 也能撤销）
 */
@RestController
@RequestMapping("/api/iam/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return Result.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody @Valid LogoutRequest req) {
        authService.logout(req);
        return Result.ok();
    }
}
