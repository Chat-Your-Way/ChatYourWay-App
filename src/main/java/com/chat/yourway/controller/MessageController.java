package com.chat.yourway.controller;

import static com.chat.yourway.config.openapi.OpenApiMessages.CONTACT_UNAUTHORIZED;
import static com.chat.yourway.config.openapi.OpenApiMessages.MESSAGE_HAS_ALREADY_REPORTED;
import static com.chat.yourway.config.openapi.OpenApiMessages.MESSAGE_NOT_FOUND;
import static com.chat.yourway.config.openapi.OpenApiMessages.SUCCESSFULLY_FOUND_MESSAGE;
import static com.chat.yourway.config.openapi.OpenApiMessages.SUCCESSFULLY_REPORTED_MESSAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.chat.yourway.dto.response.ApiErrorResponseDto;
import com.chat.yourway.dto.response.MessageResponseDto;
import com.chat.yourway.service.interfaces.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Message")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
  private final MessageService messageService;

  @Operation(summary = "Make report to message",
          responses = {
                  @ApiResponse(responseCode = "200", description = SUCCESSFULLY_REPORTED_MESSAGE,
                          content = @Content),
                  @ApiResponse(responseCode = "400", description = MESSAGE_HAS_ALREADY_REPORTED,
                          content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
                  @ApiResponse(responseCode = "404", description = MESSAGE_NOT_FOUND,
                          content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
          })
  @PostMapping("/{id}/report")
  public void reportMessage(
      @PathVariable int id, Principal principal) {
    String email = principal.getName();
    messageService.reportMessageById(id, email);
  }

  @Operation(summary = "Find all messages by topic id",
      responses = {
          @ApiResponse(responseCode = "200", description = SUCCESSFULLY_FOUND_MESSAGE),
          @ApiResponse(responseCode = "403", description = CONTACT_UNAUTHORIZED,
              content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
      })
  @GetMapping(path = "/all/{topicId}", produces = APPLICATION_JSON_VALUE)
  public List<MessageResponseDto> findAllByTopicId(@PathVariable Integer topicId){
    return messageService.findAllByTopicId(topicId);
  }
}
