package com.chat.yourway.controller.rest;

import com.chat.yourway.dto.request.AuthRequestDto;
import com.chat.yourway.dto.request.ContactRequestDto;
import com.chat.yourway.dto.request.EmailRequestDto;
import com.chat.yourway.dto.response.AuthResponseDto;
import com.chat.yourway.dto.response.RegistrationResponseDto;
import com.chat.yourway.dto.response.error.ApiErrorResponseDto;
import com.chat.yourway.exception.ContactNotFoundException;
import com.chat.yourway.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.chat.yourway.config.openapi.OpenApiMessages.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService authService;
    private static final String REGISTER = "/register";
    private static final String ACTIVE_SEND_TOKEN = "/resend/email";
    private static final String LOGIN = "/login";
    private static final String REFRESH = "/refresh";
    private static final String ACTIVATE = "/activate";
    private static final String LOGOUT = "/logout";

    @Operation(summary = "Registration a new contact", responses = {
                    @ApiResponse(responseCode = "201", description = SUCCESSFULLY_REGISTERED,
                            content = @Content(schema = @Schema(implementation = RegistrationResponseDto.class))),
                    @ApiResponse(responseCode = "409", description = VALUE_NOT_UNIQUE,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = ERR_SENDING_EMAIL,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
                }
            )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = REGISTER, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody ContactRequestDto request,
                                            @RequestHeader(HttpHeaders.REFERER) String clientHost) {
        try {
            System.out.println(clientHost);
            authService.register(request, clientHost);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User has registered successfully"));
        } catch (Exception ex) {
            System.err.println("Error during user creation: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @Operation(summary = "Authorization", responses = {
                    @ApiResponse(responseCode = "200", description = SUCCESSFULLY_AUTHORIZATION,
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = CONTACT_NOT_FOUND,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = CONTACT_UNAUTHORIZED,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
            })
    @PostMapping(path = LOGIN, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthRequestDto request) {
        try {
            AuthResponseDto response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", Objects.requireNonNull(ex.getReason())));
        } catch (ContactNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", Objects.requireNonNull(ex.getMessage())));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Щось пішло не так. Помилка сервера."));
        }
    }

    @Operation(summary = "Refresh token", responses = {
                    @ApiResponse(responseCode = "200", description = SUCCESSFULLY_REFRESHED_TOKEN,
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = CONTACT_NOT_FOUND,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = CONTACT_UNAUTHORIZED,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
            })
    @PostMapping(path = REFRESH, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public AuthResponseDto refreshToken(HttpServletRequest request) {
        return authService.refreshToken(request);
    }

    @Operation(summary = "Activate account", responses = {
            @ApiResponse(responseCode = "200", description = SUCCESSFULLY_ACTIVATED_ACCOUNT),
            @ApiResponse(responseCode = "404", description = EMAIL_TOKEN_NOT_FOUND,
                    content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping(path = ACTIVATE)
    public ResponseEntity<String> activateAccount(@RequestParam("token") String token) {
        authService.activateAccount(token); // Передаем токен для активации
        return ResponseEntity.ok("Account successfully activated");
    }


    @Operation(summary = "Resend email", responses = {
            @ApiResponse(responseCode = "200", description = SUCCESSFULLY_ACTIVATED_ACCOUNT)
    })
    @PostMapping(path = ACTIVE_SEND_TOKEN, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> activeAccountSend(@RequestBody EmailRequestDto email,
                                  @RequestHeader(HttpHeaders.REFERER) String clientHost) {
        try {
            authService.activeAccountEmailCodeLink(email, clientHost);
            return ResponseEntity.ok("Лист активації відправлено");
        } catch (Exception ex) {
            System.err.println("Error during sending activation email: " + ex.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ex.getMessage());
        }
    }

    @Operation(summary = "Logout", responses = {
                    @ApiResponse(responseCode = "200", description = SUCCESSFULLY_LOGGED_OUT),
                    @ApiResponse(responseCode = "401", description = CONTACT_UNAUTHORIZED,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
            })
    @PostMapping(value = LOGOUT, consumes = APPLICATION_JSON_VALUE)
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        authService.logout(request, response, auth);
    }
}
