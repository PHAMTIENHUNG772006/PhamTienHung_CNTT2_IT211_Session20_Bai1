package com.re.session20.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RegisterRequest {
    @NotBlank(message = "Không được để trống username")
    private String username;
    @NotBlank(message = "Không được để trống password")
    private String password;
    @NotBlank(message = "Không được để trống full name")
    private String fullName;
    @NotBlank(message = "Không được để trống email")
    private String email;
    @NotBlank(message = "Không được để trống phone")
    private String phone;
    @NotEmpty(message = "Phải có ít nhất 1 role")
    private Set<String> roles;
}
