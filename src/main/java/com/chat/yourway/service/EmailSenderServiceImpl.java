package com.chat.yourway.service;

import static org.springframework.http.HttpStatus.*;

import com.chat.yourway.exception.ServiceException;
import com.chat.yourway.model.email.EmailSend;
import com.chat.yourway.service.interfaces.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@EnableAsync
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

  @Value("${spring.mail.username}")
  private String emailAddressFrom;
  private final JavaMailSender javaMailSender;

  @Async
  @Override
  public void sendEmail(EmailSend request) {
      if (Objects.isNull(request)) {
          return;
      }

    MimeMessage mail = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mail);

    try {
      helper.setFrom(emailAddressFrom);
      helper.setTo(request.to());
      helper.setSubject(request.subject());
      helper.setText(request.text());
      javaMailSender.send(mail);
    } catch (MessagingException e) {
      throw new ServiceException(BAD_REQUEST, "Error sending email");
    }
  }
}
