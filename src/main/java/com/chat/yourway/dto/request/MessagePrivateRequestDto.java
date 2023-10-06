package com.chat.yourway.dto.request;

import com.chat.yourway.annotation.EmailValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessagePrivateRequestDto {

  @Schema(description = "Contact email for sending message", example = "contact@gmail.com")
  @EmailValidation
  private String sendTo;
  @Schema(description = "Message content", example = "Hello world!")
  @NotEmpty(message = "Message content cannot be empty")
  @Size(min = 2, max = 500, message = "Message content length should be from 2 to 500 symbols")
  private String content;

}
