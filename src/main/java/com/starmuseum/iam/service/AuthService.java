package com.starmuseum.iam.service;

import com.starmuseum.iam.dto.LoginRequest;
import com.starmuseum.iam.dto.RefreshRequest;
import com.starmuseum.iam.dto.RegisterRequest;
import com.starmuseum.iam.dto.LogoutRequest;
import com.starmuseum.iam.vo.AuthResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    void register(RegisterRequest req);

    AuthResponse login(LoginRequest req);

    AuthResponse refresh(RefreshRequest req);

    void logout(LogoutRequest req);
}
