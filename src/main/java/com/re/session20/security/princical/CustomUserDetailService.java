package com.re.session20.security.princical;

import com.re.session20.model.entity.Employee;
import com.re.session20.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee =
                employeeRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Không tồn tại tài khoản: " + username
                                ));

        List<SimpleGrantedAuthority> authorities =
                employee.getRoles()
                        .stream()
                        .map(role ->
                                new SimpleGrantedAuthority(
                                        role.getRoleName()
                                ))
                        .toList();

        return CustomUserDetails.builder()
                .username(employee.getUsername())
                .password(employee.getPassword())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .authorities(authorities)
                .build();
    }
}
