package com.re.session20.model.dto.response;
import com.re.session20.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String username;
    private String fullName;
    private boolean enabled;
    private List<Role> authorities;
}