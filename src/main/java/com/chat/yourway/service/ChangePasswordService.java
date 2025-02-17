package com.chat.yourway.service;

import com.chat.yourway.config.security.MyPasswordEncoder;
import com.chat.yourway.dto.common.EmailMessageInfoDto;
import com.chat.yourway.dto.request.ChangePasswordDto;
import com.chat.yourway.dto.request.RestorePasswordDto;
import com.chat.yourway.exception.EmailTokenNotFoundException;
import com.chat.yourway.model.Contact;
import com.chat.yourway.model.enums.EmailMessageType;
import com.chat.yourway.model.EmailToken;
import com.chat.yourway.repository.jpa.EmailTokenRepository;
import com.chat.yourway.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangePasswordService {

    private final MyPasswordEncoder myPasswordEncoder;
    private final ContactService contactService;
    private final EmailTokenRepository emailTokenRepository;
    private final EmailMessageFactoryService emailMessageFactoryService;
    private final EmailSenderService emailSenderService;
    private final JwtService jwtService;

    @Transactional
    public void changePassword(ChangePasswordDto request) {
        Contact contact = contactService.getCurrentContact();
        contactService.verifyPassword(request.getOldPassword(), contact.getPassword());
        contactService.changePasswordByEmail(request.getNewPassword(), contact.getEmail());
    }

    @Transactional
    public void sendEmailToRestorePassword(String email, String clientHost) {
        var contact = contactService.findByEmail(email);
        var emailToken = EmailToken.builder()
                        .messageType(EmailMessageType.RESTORE_PASSWORD)
                        .contact(contact)
                        .build();

        if (emailTokenRepository.findByContact(contact).isEmpty()){
            emailTokenRepository.save(emailToken);
        }

        var emailMessageInfo = new EmailMessageInfoDto(
                contact.getNickname(),
                contact.getEmail(),
                clientHost,
                EmailMessageType.RESTORE_PASSWORD);
        var emailMessage = emailMessageFactoryService.generateEmailMessage(emailMessageInfo);

        emailSenderService.sendEmail(emailMessage);
    }

    @Transactional
    public void restorePassword(RestorePasswordDto restorePasswordDto) {

        String token = restorePasswordDto.getEmailToken();
        String email = jwtService.extractEmail(token);

        var contact = contactService.findByEmail(email);
        var emailToken = emailTokenRepository.findByContact(contact)
                .orElseThrow(EmailTokenNotFoundException::new);

        var newEncodedPassword = myPasswordEncoder.encode(restorePasswordDto.getNewPassword());

        contact.setPassword(newEncodedPassword);
        emailTokenRepository.delete(emailToken);
    }

}
