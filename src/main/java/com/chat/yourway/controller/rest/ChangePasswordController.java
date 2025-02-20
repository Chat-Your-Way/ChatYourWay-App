package com.chat.yourway.controller.rest;

import com.chat.yourway.dto.request.ChangePasswordDto;
import com.chat.yourway.dto.request.RestorePasswordDto;
import com.chat.yourway.dto.response.error.ApiErrorResponseDto;
import com.chat.yourway.service.ChangePasswordService;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.chat.yourway.config.openapi.OpenApiMessages.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Change password")
@RestController
@RequestMapping("/change")
@RequiredArgsConstructor
public class ChangePasswordController {

    private final ChangePasswordService changePasswordService;
    private static final String PASSWORD = "/password";
    private static final String PASSWORD_EMAIL = "/password/email";
    private static final String PASSWORD_RESTORE = "/password/restore";

    @Operation(summary = "Change to new password", responses = {
                    @ApiResponse(responseCode = "200", description = SUCCESSFULLY_CHANGING_PASSWORD),
                    @ApiResponse(responseCode = "400", description = INVALID_OLD_PASSWORD,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
            })
    @PatchMapping(path = PASSWORD)
    public void changePassword(@Valid @RequestBody ChangePasswordDto request) {
        changePasswordService.changePassword(request);
    }

    @Operation(summary = "Send email to restore password", responses = {
                    @ApiResponse(responseCode = "200", description = SUCCESSFULLY_SEND_REQUEST_RESTORE_PASSWORD),
                    @ApiResponse(responseCode = "400", description = ERR_SENDING_EMAIL,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
            })
    @PostMapping(path = PASSWORD_EMAIL)
    public void sendRequestToRestorePassword(@RequestBody String email,
                                             @RequestHeader(HttpHeaders.REFERER) String clientHost) {
        email = email.replaceAll("^\"|\"$", "");
        changePasswordService.sendEmailToRestorePassword(email, clientHost);
    }

    @Operation(summary = "Restore password", responses = {
                    @ApiResponse(responseCode = "200", description = SUCCESSFULLY_RESTORED_PASSWORD,
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = EMAIL_TOKEN_NOT_FOUND,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
            })
    @PatchMapping(path = PASSWORD_RESTORE)
    public ResponseEntity<String> restorePassword(@Valid @RequestBody RestorePasswordDto restorePasswordDto) {
    try {
        changePasswordService.restorePassword(restorePasswordDto);
        return ResponseEntity.ok("Пароль успішно змінено");
    } catch (Exception ex) {
        System.err.println("Помилка при зміні паролю: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
        }
    }
}