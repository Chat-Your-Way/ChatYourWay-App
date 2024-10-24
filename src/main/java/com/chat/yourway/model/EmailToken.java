package com.chat.yourway.model;

import com.chat.yourway.model.enums.EmailMessageType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(schema = "chat", name = "email_tokens")
public class EmailToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID token;

  @Enumerated(EnumType.STRING)
  private EmailMessageType messageType;

  @OneToOne
  @JoinColumn(name = "contact_id", referencedColumnName = "id")
  private Contact contact;
}
