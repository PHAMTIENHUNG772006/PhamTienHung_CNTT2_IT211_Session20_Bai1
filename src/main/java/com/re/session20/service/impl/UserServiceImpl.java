package com.re.session20.service.impl;

import com.re.session20.model.custom_exception.UserNotFoundException;
import com.re.session20.model.dto.request.LoginRequest;
import com.re.session20.model.dto.request.RefreshTokenRequest;
import com.re.session20.model.dto.request.RegisterRequest;
import com.re.session20.model.dto.response.JwtResponse;
import com.re.session20.model.dto.response.RefreshTokenResponse;
import com.re.session20.model.entity.Employee;
import com.re.session20.model.entity.Token;
import com.re.session20.model.entity.TokenType;
import com.re.session20.repository.EmployeeRepository;
import com.re.session20.repository.TokenRepository;
import com.re.session20.security.jwt.JwtProvider;
import com.re.session20.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final EmployeeRepository employeeRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    @Override
    public Employee findByUsername(String username) {
        return employeeRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Không thấy User cần tìm"));
    }

    @Override
    public Employee register(RegisterRequest registerRequest) {

        Employee user = Employee.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(
                        passwordEncoder.encode(
                                registerRequest.getPassword()
                        )
                )
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .enabled(true)
                .build();

        return employeeRepository.save(user);
    }

    @Override
    public JwtResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        Employee employee = employeeRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy user"));

        String accessToken = jwtProvider.generateAccessToken(employee);

        String refreshToken = jwtProvider.generateRefreshToken(employee);

        saveToken(employee, accessToken, TokenType.ACCESS);

        saveToken(employee, refreshToken, TokenType.REFRESH);

        return JwtResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .username(employee.getUsername())
                .fullName(employee.getFullName())
                .enabled(employee.getEnabled())
                .authorities(employee.getRoles())
                .build();
    }

    private void saveToken(
            Employee employee,
            String tokenValue,
            TokenType tokenType
    ){

        Token token = Token.builder()
                .tokenValue(tokenValue)
                .tokenType(tokenType)
                .revoked(false)
                .expired(false)
                .employee(employee)
                .build();

        tokenRepository.save(token);
    }

    @Override
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {

        Token refreshToken =
                tokenRepository
                        .findByTokenValue(
                                request.getRefreshToken()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Refresh token không tồn tại"
                                )
                        );

        if (refreshToken.getRevoked()
                || refreshToken.getExpired()) {

            throw new RuntimeException(
                    "Refresh token đã bị thu hồi"
            );
        }

        Employee employee =
                refreshToken.getEmployee();

        String newAccessToken =
                jwtProvider.generateAccessToken(employee);

        tokenRepository.save(
                Token.builder()
                        .tokenValue(newAccessToken)
                        .tokenType(TokenType.ACCESS)
                        .revoked(false)
                        .expired(false)
                        .employee(employee)
                        .build()
        );

        return new RefreshTokenResponse(
                newAccessToken
        );
    }

    @Override
    public void logout(String username) {

        Employee employee =
                employeeRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new UserNotFoundException("User not found")
                        );

        List<Token> validTokens =
                tokenRepository
                        .findAllValidTokensByEmployee(
                                employee.getId()
                        );

        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });

        tokenRepository.saveAll(validTokens);

        SecurityContextHolder.clearContext();
    }
}
