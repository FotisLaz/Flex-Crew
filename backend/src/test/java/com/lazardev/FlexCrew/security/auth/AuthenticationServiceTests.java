package com.lazardev.FlexCrew.security.auth;

import com.lazardev.FlexCrew.dao.EmployeeRepository;
import com.lazardev.FlexCrew.entity.Employee;
import com.lazardev.FlexCrew.entity.Role;
import com.lazardev.FlexCrew.security.config.JwtService;
import com.lazardev.FlexCrew.security.token.Token;
import com.lazardev.FlexCrew.security.token.TokenRepository;
import com.lazardev.FlexCrew.security.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<Employee> employeeArgumentCaptor;
    @Captor
    private ArgumentCaptor<Token> tokenArgumentCaptor;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private Employee employee;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .names("Test")
                .firstSurname("User")
                .secondSurname("Dev")
                .email("test.user@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        authRequest = AuthenticationRequest.builder()
                .email("test.user@example.com")
                .password("password123")
                .build();

        employee = Employee.builder()
                .id(1)
                .names("Test")
                .firstSurname("User")
                .email("test.user@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_shouldSaveUserAndTokens_whenEmailNotExists() {
        when(employeeRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee); // Return the employee with an ID
        when(jwtService.generateToken(any(Employee.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(Employee.class))).thenReturn("refreshToken");

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals(Role.USER, response.getRole());

        verify(employeeRepository).save(employeeArgumentCaptor.capture());
        Employee savedEmployee = employeeArgumentCaptor.getValue();
        assertEquals(registerRequest.getEmail(), savedEmployee.getEmail());
        assertEquals("encodedPassword", savedEmployee.getPassword());
        assertEquals(Role.USER, savedEmployee.getRole());

        verify(tokenRepository, times(1)).save(tokenArgumentCaptor.capture());
        Token savedToken = tokenArgumentCaptor.getValue();
        assertEquals("accessToken", savedToken.getToken());
        assertEquals(TokenType.BEARER, savedToken.getTokenType());
        assertFalse(savedToken.isExpired());
        assertFalse(savedToken.isRevoked());
        assertEquals(employee, savedToken.getEmployee());
    }

    @Test
    void register_shouldThrowRuntimeException_whenEmailAlreadyExists() {
        when(employeeRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(employee));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void authenticate_shouldReturnTokensAndRevokeOldOnes_whenCredentialsAreValid() {
        when(employeeRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(employee));
        when(jwtService.generateToken(employee)).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(employee)).thenReturn("newRefreshToken");

        // Mock finding valid old tokens
        Token oldToken1 = Token.builder().id(1).token("oldAccess").employee(employee).expired(false).revoked(false).build();
        when(tokenRepository.findAllValidTokenByUser(employee.getId())).thenReturn(Collections.singletonList(oldToken1));

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        assertEquals(Role.USER, response.getRole());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

        verify(tokenRepository).findAllValidTokenByUser(employee.getId());
        verify(tokenRepository).saveAll(anyList()); // Verifies that saveAll for revoking tokens was called

        // Verify new token is saved
        verify(tokenRepository, times(1)).save(tokenArgumentCaptor.capture());
        Token savedToken = tokenArgumentCaptor.getValue();
        assertEquals("newAccessToken", savedToken.getToken());
        assertEquals(employee, savedToken.getEmployee());
    }


    @Test
    void authenticate_shouldThrowException_whenUserNotFound() {
        when(employeeRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        // AuthenticationManager will throw an exception if user not found (handled by DaoAuthenticationProvider)
        // So, we can assert that the repository was called, and expect AuthenticationManager to handle the rest.
        // Or, more directly, mock the AuthenticationManager to throw an error if that's desired.
        
        // For this test, we'll assume AuthenticationManager handles the exception correctly when user is not found based on its configuration.
        // The primary check here is that we attempted to find the user.
        assertThrows(Exception.class, () -> {
             authenticationManager.authenticate( // Simulate what happens if auth manager is called
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
             // If the above doesn't throw due to mock setup, call the actual service method after setting up repo mock
             authenticationService.authenticate(authRequest);
        });
        
        // Verify that employeeRepository.findByEmail was indeed called
        verify(employeeRepository).findByEmail(authRequest.getEmail());
    }
} 