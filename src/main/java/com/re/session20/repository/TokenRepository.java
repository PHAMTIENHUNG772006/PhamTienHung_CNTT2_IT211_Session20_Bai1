package com.re.session20.repository;

import com.re.session20.model.entity.Token;
import com.re.session20.model.entity.Employee;
import com.re.session20.model.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValue(String tokenValue);

    List<Token> findByEmployee(Employee employee);

    List<Token> findByEmployeeAndTokenType(
            Employee employee,
            TokenType tokenType
    );

    @Query("""
            select t
            from Token t
            where t.employee.id = :employeeId
            and t.revoked = false
            and t.expired = false
            """)
    List<Token> findAllValidTokensByEmployee(Long employeeId);

    Optional<Token> findByTokenValueAndTokenType(
            String tokenValue,
            TokenType tokenType
    );
}