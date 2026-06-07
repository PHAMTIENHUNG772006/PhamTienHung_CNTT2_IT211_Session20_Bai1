package com.re.session20.security.jwt;

import com.re.session20.model.dto.response.ApiDataResponse;
import com.re.session20.repository.TokenRepository;
import com.re.session20.security.princical.CustomUserDetailService;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;
    private final CustomUserDetailService userDetailService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String token = parseJwt(request);

            if(token != null){

                boolean validTokenInDb =
                        tokenRepository
                                .findByTokenValue(token)
                                .filter(t -> !t.getRevoked())
                                .filter(t -> !t.getExpired())
                                .isPresent();

                if(validTokenInDb
                        && jwtProvider.validateToken(token)){

                    String username =
                            jwtProvider.getUsernameFromToken(token);

                    UserDetails userDetails =
                            userDetailService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request,response);

        } catch (ExpiredJwtException e) {

            sendErrorResponse(
                    response,
                    "Access Token đã hết hạn",
                    HttpStatus.UNAUTHORIZED
            );

        } catch (JwtException e) {

            sendErrorResponse(
                    response,
                    "Token không hợp lệ",
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    private String parseJwt(HttpServletRequest request) {

        String headerAuth =
                request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth)
                && headerAuth.startsWith("Bearer ")) {

            return headerAuth.substring(7);
        }

        return null;
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            String message,
            HttpStatus status
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiDataResponse<Void> apiResponse =
                new ApiDataResponse<>(
                        false,
                        message,
                        null,
                        null,
                        status
                );

        ObjectMapper mapper = new ObjectMapper();

        response.getWriter()
                .write(mapper.writeValueAsString(apiResponse));
    }
}