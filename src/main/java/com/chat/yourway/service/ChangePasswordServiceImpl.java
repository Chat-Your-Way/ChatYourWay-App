package com.chat.yourway.service;

import com.chat.yourway.dto.common.EmailMessageInfoDto;
import com.chat.yourway.dto.request.ChangePasswordDto;
import com.chat.yourway.dto.request.RestorePasswordDto;
import com.chat.yourway.exception.EmailTokenNotFoundException;
import com.chat.yourway.model.email.EmailMessageType;
import com.chat.yourway.model.email.EmailToken;
import com.chat.yourway.repository.EmailTokenRepository;
import com.chat.yourway.service.interfaces.ChangePasswordService;
import com.chat.yourway.service.interfaces.ContactService;
import com.chat.yourway.service.interfaces.EmailMessageFactoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangePasswordServiceImpl implements ChangePasswordService {

  private final PasswordEncoder passwordEncoder;
  private final ContactService contactService;
  private final EmailTokenRepository emailTokenRepository;
  private final EmailMessageFactoryService emailMessageFactoryService;
  private final EmailSenderService emailSenderService;

  @Override
  @Transactional
  public void changePassword(ChangePasswordDto request, UserDetails userDetails) {
    contactService.verifyPassword(request.getOldPassword(), userDetails.getPassword());
    contactService.changePasswordByEmail(request.getNewPassword(), userDetails.getUsername());
  }

  @Override
  @Transactional
  public void sendEmailToRestorePassword(String email, String clientHost) {

    var contact = contactService.findByEmail(email);
    var uuidToken = UUID.randomUUID().toString();
    var emailToken =
        EmailToken.builder()
            .token(uuidToken)
            .messageType(EmailMessageType.RESTORE_PASSWORD)
            .contact(contact)
            .build();

    emailTokenRepository.save(emailToken);

    var emailMessageInfo = new EmailMessageInfoDto(contact.getNickname(),
        contact.getEmail(),
        uuidToken,
        clientHost,
        EmailMessageType.RESTORE_PASSWORD);
    var emailMessage = emailMessageFactoryService.generateEmailMessage(emailMessageInfo);

    emailSenderService.sendEmail(emailMessage);
  }

  @Override
  @Transactional
  public void restorePassword(RestorePasswordDto restorePasswordDto) {
    var emailToken = emailTokenRepository.findById(restorePasswordDto.getEmailToken())
        .orElseThrow(EmailTokenNotFoundException::new);
    var contact = emailToken.getContact();
    var newEncodedPassword = passwordEncoder.encode(restorePasswordDto.getNewPassword());

    contact.setPassword(newEncodedPassword);
    emailTokenRepository.delete(emailToken);
  }

}
