package com.chat.yourway.integration.service.impl;

import com.chat.yourway.dto.request.EditContactProfileRequestDto;
import com.chat.yourway.exception.ContactNotFoundException;
import com.chat.yourway.integration.extension.PostgresExtension;
import com.chat.yourway.integration.extension.RedisExtension;
import com.chat.yourway.model.Contact;
import com.chat.yourway.repository.ContactRepository;
import com.chat.yourway.service.ContactServiceImpl;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({PostgresExtension.class,
        RedisExtension.class})
@SpringBootTest
@TestExecutionListeners(value = {
        TransactionalTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        MockitoTestExecutionListener.class,
        ResetMocksTestExecutionListener.class
})
public class ContactServiceImplTest {
    @Autowired
    ContactRepository contactRepository;
    @Autowired
    ContactServiceImpl contactService;

    @Test
    @DisplayName("should update contact profile when user is exist")
    @DatabaseSetup(value = "/dataset/contacts.xml", type = DatabaseOperation.INSERT)
    @DatabaseTearDown(value = "/dataset/contacts.xml", type = DatabaseOperation.DELETE)
    public void shouldUpdateContactProfile_whenUserIsExist() {
        // Given
        var email = "user@gmail.com";
        var editedNickname = "editedNickname";
        var editedAvatarId = (byte) 2;
        var request = new EditContactProfileRequestDto(editedNickname, editedAvatarId);
        var userDetails = Mockito.mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(email);

        // When
        contactService.updateContactProfile(request, userDetails);
        var updatedContact = contactRepository.findByEmailIgnoreCase(email).get();

        // Then
        assertAll(
                () -> assertThat(updatedContact)
                        .withFailMessage("Expecting nickname is updated")
                        .extracting(Contact::getNickname)
                        .isEqualTo(editedNickname),
                () -> assertThat(updatedContact)
                        .withFailMessage("Expecting avatar_id is updated")
                        .extracting(Contact::getAvatarId)
                        .isEqualTo(editedAvatarId));
    }

    @Test
    @DisplayName(
            "should throw ContactNotFoundException when user is not exist")
    @DatabaseSetup(value = "/dataset/contacts.xml", type = DatabaseOperation.INSERT)
    @DatabaseTearDown(value = "/dataset/contacts.xml", type = DatabaseOperation.DELETE)
    public void shouldThrowContactNotFoundException_whenUserIsNotExist() {
        // Given
        var email = "test@example.com";
        var editedNickname = "editedNickname";
        var editedAvatarId = (byte) 2;
        var request = new EditContactProfileRequestDto(editedNickname, editedAvatarId);
        var existingContact = Mockito.mock(Contact.class);
        var userDetails = Mockito.mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(email);

        // When
        assertThrows(
                ContactNotFoundException.class,
                () -> contactService.updateContactProfile(request, userDetails));

        // Then
        verify(existingContact, never()).setNickname(anyString());
        verify(existingContact, never()).setAvatarId(anyByte());
    }

    @Test
    @DisplayName("should get contact profile when user is exist")
    @DatabaseSetup(value = "/dataset/contacts.xml", type = DatabaseOperation.INSERT)
    @DatabaseTearDown(value = "/dataset/contacts.xml", type = DatabaseOperation.DELETE)
    public void shouldGetContactProfile_whenUserIsExist() {
        // Given
        var email = "user@gmail.com";
        var userDetails = Mockito.mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(email);

        // When
        contactService.getContactProfile(userDetails);
        var foundContact = contactRepository.findByEmailIgnoreCase(email).get();

        // Then
        assertAll(
                () -> assertThat(foundContact)
                        .withFailMessage("Expecting nickname is present")
                        .extracting(Contact::getNickname)
                        .isNotNull(),
                () -> assertThat(foundContact)
                        .withFailMessage("Expecting avatar id is present")
                        .extracting(Contact::getAvatarId)
                        .isNotNull(),
                () -> assertThat(foundContact)
                        .withFailMessage("Expecting permission of sending private message is present")
                        .extracting(Contact::isPermittedSendingPrivateMessage)
                        .isNotNull());
    }

    @Test
    @DisplayName(
            "should throw ContactNotFoundException when contact profile is not exist")
    @DatabaseSetup(value = "/dataset/contacts.xml", type = DatabaseOperation.INSERT)
    @DatabaseTearDown(value = "/dataset/contacts.xml", type = DatabaseOperation.DELETE)
    public void shouldThrowContactNotFoundException_whenContactProfileIsNotExist() {
        // Given
        var email = "test@example.com";
        var existingContact = Mockito.mock(Contact.class);
        var userDetails = Mockito.mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(email);

        // When
        assertThrows(
                ContactNotFoundException.class,
                () -> contactService.getContactProfile(userDetails));

        // Then
        verify(existingContact, never()).setNickname(anyString());
        verify(existingContact, never()).setAvatarId(anyByte());
        verify(existingContact, never()).setPermittedSendingPrivateMessage(anyBoolean());
    }


    @Test
    @DatabaseSetup(
            value = "/dataset/permission-sending-message-in-private-topic.xml",
            type = DatabaseOperation.INSERT)
    @DatabaseTearDown(
            value = "/dataset/permission-sending-message-in-private-topic.xml",
            type = DatabaseOperation.DELETE)
    @DisplayName(
            "should successfully permit sending private messages when user change permission")
    public void shouldSuccessfullyPermitSendingPrivateMessages_whenUserChangePermission() {
        // Given
        var contactEmail = "vasil299@gmail.com";
        var contact = contactService.findByEmail(contactEmail);

        // When
        contactService.permitSendingPrivateMessages(contact);

        // Then
        var result = contactService.findByEmail(contactEmail);

        assertThat(result)
                .extracting(Contact::isPermittedSendingPrivateMessage)
                .isEqualTo(true);
    }

    @Test
    @DatabaseSetup(
            value = "/dataset/permission-sending-message-in-private-topic.xml",
            type = DatabaseOperation.INSERT)
    @DatabaseTearDown(
            value = "/dataset/permission-sending-message-in-private-topic.xml",
            type = DatabaseOperation.DELETE)
    @DisplayName(
            "should successfully prohibit sending private messages when user change permission")
    public void shouldSuccessfullyProhibitSendingPrivateMessages_whenUserChangePermission() {
        // Given
        var contactEmail = "vasil299@gmail.com";
        var contact = contactService.findByEmail(contactEmail);

        // When
        contactService.prohibitSendingPrivateMessages(contact);

        // Then
        var result = contactRepository.findByEmailIgnoreCase(contactEmail).get();

        assertThat(result)
                .extracting(Contact::isPermittedSendingPrivateMessage)
                .isEqualTo(false);
    }

    @Test
    @DatabaseSetup(
            value = "/dataset/permission-sending-message-in-private-topic.xml",
            type = DatabaseOperation.INSERT)
    @DatabaseTearDown(
            value = "/dataset/permission-sending-message-in-private-topic.xml",
            type = DatabaseOperation.DELETE)
    @DisplayName(
            "should throw ContactNotFoundException when user change permission")
    public void shouldThrowContactNotFoundException_whenUserChangePermission() {
        // Given
        var contactEmail = "vasil2929@gmail.com";
        Contact contact = new Contact();

        contact.setEmail(contactEmail);

        // When
        // Then
        assertThrows(
                ContactNotFoundException.class,
                () -> contactService.permitSendingPrivateMessages(contact));
    }
}
