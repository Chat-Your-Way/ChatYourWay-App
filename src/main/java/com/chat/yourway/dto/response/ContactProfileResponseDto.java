package com.chat.yourway.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ContactProfileResponseDto {

    private UUID id;
    private String nickname;
    private String email;
    private Byte avatarId;
    private Boolean hasPermissionSendingPrivateMessage;
    private LocalDateTime createdAt;
}
