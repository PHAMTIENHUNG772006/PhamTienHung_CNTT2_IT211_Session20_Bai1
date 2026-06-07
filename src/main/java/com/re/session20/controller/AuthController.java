package com.re.session20.controller;

import com.re.session20.model.dto.request.LoginRequest;
import com.re.session20.model.dto.request.RefreshTokenRequest;
import com.re.session20.model.dto.request.RegisterRequest;
import com.re.session20.model.dto.response.ApiDataResponse;
import com.re.session20.model.dto.response.JwtResponse;
import com.re.session20.model.dto.response.RefreshTokenResponse;
import com.re.session20.model.entity.Employee;
import com.re.session20.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiDataResponse<Employee>> registerUser(
            @Valid @RequestBody RegisterRequest userDTO
    ) {

        Employee employee =
                userService.register(userDTO);

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Đăng ký tài khoản "
                                + userDTO.getUsername()
                                + " thành công",
                        employee,
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiDataResponse<JwtResponse>> login(
            @RequestBody LoginRequest loginRequest
    ) {

        JwtResponse jwtResponse =
                userService.login(loginRequest);

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Đăng nhập thành công",
                        jwtResponse,
                        null,
                        HttpStatus.OK
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiDataResponse<RefreshTokenResponse>> refresh(
            @RequestBody RefreshTokenRequest request
    ) {

        RefreshTokenResponse refreshResponse =
                userService.refresh(request);

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Cấp lại Access Token thành công",
                        refreshResponse,
                        null,
                        HttpStatus.OK
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiDataResponse<Void>> logout(
            Authentication authentication
    ) {

        userService.logout(
                authentication.getName()
        );

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Đăng xuất thành công",
                        null,
                        null,
                        HttpStatus.OK
                ),
                HttpStatus.OK
        );
    }
}