package com.chat.yourway.repository.jpa;

import com.chat.yourway.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

  Optional<Contact> findByEmailIgnoreCase(String email);

  @Modifying
  @Query("UPDATE Contact c set c.password = :password where c.email = :email")
  void changePasswordByEmail(String password, String email);

  boolean existsByEmailIgnoreCase(String email);

  @Modifying
  @Query(nativeQuery = true, value = """
                UPDATE chat.contacts SET is_permitted_sending_private_message = :isPermittedSendingPrivateMessage
                          WHERE email = :contactEmail
                          """)
  void updatePermissionSendingPrivateMessageByContactEmail(
          @Param("contactEmail") String contactEmail,
          @Param("isPermittedSendingPrivateMessage") boolean isPermittedSendingPrivateMessage);

  @Modifying
  @Query(nativeQuery = true, value = """
    UPDATE chat.contacts
    SET is_deleted = TRUE,
        nickname = CONCAT('Видаленний користувач_', LEFT(gen_random_uuid()::TEXT, 8)),
        avatar_id = 13,
        is_active = FALSE
    WHERE id = :contactId
""")
  void markUserAsDeleted(@Param("contactId") UUID contactId);

  @Modifying
  @Query(nativeQuery = true, value = """
    WITH updated_contact AS (
        UPDATE chat.contacts
        SET is_deleted = TRUE,
            nickname = CONCAT('Видаленний користувач_', LEFT(gen_random_uuid()::TEXT, 8)),
            avatar_id = 13,
            is_active = FALSE
        WHERE id = :contactId
        RETURNING nickname
    )
    UPDATE chat.topics
    SET contact_nickname = (SELECT nickname FROM updated_contact)
    WHERE contact_nickname = (SELECT nickname FROM chat.contacts WHERE id = :contactId)
    AND EXISTS (SELECT 1 FROM updated_contact);
""")
  void markUserAsDeletedWithTopics(@Param("contactId") UUID contactId);

  @Modifying
  @Query(nativeQuery = true, value =
          "WITH updated_contact AS (" +
                  "    UPDATE chat.contacts " +
                  "    SET nickname = :newNickname " +
                  "    WHERE nickname = :oldNickname " +
                  "    RETURNING nickname " +
                  ") " +
                  "UPDATE chat.topics " +
                  "SET contact_nickname = :newNickname " +
                  "WHERE contact_nickname = :oldNickname " +
                  "AND EXISTS (SELECT 1 FROM updated_contact)")
  void updateNicknameInContactsAndTopics(@Param("oldNickname") String oldNickname, @Param("newNickname") String newNickname);

  List<Contact> findAllByIsActiveFalseAndCreatedAtBefore(LocalDateTime createdAt);
}
