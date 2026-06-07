package com.re.session20.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;
    @NotBlank(message = "mật khẩu không được để trống")
    private String password;
}
