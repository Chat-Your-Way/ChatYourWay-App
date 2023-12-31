package com.chat.yourway.dto.common;

import com.chat.yourway.model.email.EmailMessageType;

public record EmailMessageInfoDto(String username,
                                  String email,
                                  String uuidToken,
                                  String path,
                                  EmailMessageType emailMessageType) {

}
