package com.chat.yourway.service;

import com.chat.yourway.config.security.MyPasswordEncoder;
import com.chat.yourway.dto.request.ContactRequestDto;
import com.chat.yourway.dto.request.EditContactProfileRequestDto;
import com.chat.yourway.dto.response.ContactProfileResponseDto;
import com.chat.yourway.dto.response.ContactResponseDto;
import com.chat.yourway.exception.ContactNotFoundException;
import com.chat.yourway.exception.PasswordsAreNotEqualException;
import com.chat.yourway.exception.ValueNotUniqException;
import com.chat.yourway.model.Contact;
import com.chat.yourway.model.Message;
import com.chat.yourway.repository.jpa.ContactRepository;
import com.chat.yourway.repository.jpa.TopicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.chat.yourway.model.enums.Role.USER;

@Service
@Slf4j
public class ContactService {

    private final ContactOnlineService contactOnlineService;
    private final ContactRepository contactRepository;
    private final MyPasswordEncoder myPasswordEncoder;
    private final TopicRepository topicRepository;

    public ContactService(@Lazy ContactOnlineService contactOnlineService,
                          ContactRepository contactRepository, TopicRepository topicRepository,
                          MyPasswordEncoder myPasswordEncoder) {
        this.contactOnlineService = contactOnlineService;
        this.contactRepository = contactRepository;
        this.myPasswordEncoder = myPasswordEncoder;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public void save(Contact contact) {
        contactRepository.save(contact);
    }

    public List<ContactResponseDto> findAllOnlineContacts() {
        return contactOnlineService.getOnlineContactsDto();
    }

    public List<ContactResponseDto> findAllOnlineContactsByTopicId(UUID topicId) {
        return contactOnlineService.getOnlineUsersByTopicId(topicId);
    }

//    @Transactional
//    public Contact create(ContactRequestDto contactRequestDto) {
//        log.trace("Started create contact, contact email: [{}]", contactRequestDto.getEmail());
//
//        Optional<Contact> existingContact = contactRepository.findByEmailIgnoreCase(contactRequestDto.getEmail());
//
//        if (existingContact.isPresent()) {
//            Contact contact = existingContact.get();
//
//            if (contact.isDeleted()) {
//                log.info("Email [{}] was previously deleted. Restoring account.", contactRequestDto.getEmail());
//
//                contact.setNickname(contactRequestDto.getNickname());
//                contact.setAvatarId(contactRequestDto.getAvatarId());
//                contact.setPassword(myPasswordEncoder.encode(contactRequestDto.getPassword()));
//                contact.setActive(false);
//                contact.setDeleted(false);
//                contact.setPermittedSendingPrivateMessage(true);
//
//                contactRepository.save(contact);
//                return contact;
//            }
//
//            log.warn("Email [{}] already in use", contactRequestDto.getEmail());
//            throw new ValueNotUniqException(
//                    String.format("Електронна пошта [%s] вже використовується", contactRequestDto.getEmail())
//            );
//        }
//
//        Contact newContact = Contact.builder()
//                .nickname(contactRequestDto.getNickname())
//                .avatarId(contactRequestDto.getAvatarId())
//                .email(contactRequestDto.getEmail())
//                .password(myPasswordEncoder.encode(contactRequestDto.getPassword()))
//                .isActive(false)
//                .role(USER)
//                .isPermittedSendingPrivateMessage(true)
//                .build();
//
//        contactRepository.save(newContact);
//        log.info("New contact with email [{}] was created", contactRequestDto.getEmail());
//        return newContact;
//    }

    @Transactional
    public Contact create(ContactRequestDto contactRequestDto) {
        log.trace("Started create contact, contact email: [{}]", contactRequestDto.getEmail());

        Optional<Contact> existingContact = contactRepository.findByEmailIgnoreCase(contactRequestDto.getEmail());

        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();

            if (contact.isDeleted()) {
                log.info("Email [{}] was previously deleted. Restoring account.", contactRequestDto.getEmail());

                String oldNickname = contact.getNickname();
                String newNickname = contactRequestDto.getNickname();

                // Обновляем никнейм в таблице topics перед восстановлением
                contactRepository.updateNicknameInContactsAndTopics(oldNickname, newNickname);

                contact.setNickname(newNickname);
                contact.setAvatarId(contactRequestDto.getAvatarId());
                contact.setPassword(myPasswordEncoder.encode(contactRequestDto.getPassword()));
                contact.setActive(false);
                contact.setDeleted(false);
                contact.setPermittedSendingPrivateMessage(true);

                contactRepository.save(contact);
                log.info("Successfully restored account for [{}]", contactRequestDto.getEmail());

                return contact;
            }

            log.warn("Email [{}] already in use", contactRequestDto.getEmail());
            throw new ValueNotUniqException(
                    String.format("Електронна пошта [%s] вже використовується", contactRequestDto.getEmail())
            );
        }

        Contact newContact = Contact.builder()
                .nickname(contactRequestDto.getNickname())
                .avatarId(contactRequestDto.getAvatarId())
                .email(contactRequestDto.getEmail())
                .password(myPasswordEncoder.encode(contactRequestDto.getPassword()))
                .isActive(false)
                .role(USER)
                .isPermittedSendingPrivateMessage(true)
                .build();

        contactRepository.save(newContact);
        log.info("New contact with email [{}] was created", contactRequestDto.getEmail());

        return newContact;
    }


    @Transactional(readOnly = true)
    public Contact findByEmail(String email) {
        log.trace("Started findByEmail: [{}]", email);
        Contact contact = contactRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("Email [{}] wasn't found", email);
                    return new ContactNotFoundException(String.format("Пошта [%s] не знайдена", email));
                    }
                );

        log.info("Contact was found by email [{}]", email);
        return contact;
    }

    @Transactional
    public void changePasswordByEmail(String password, String email) {
        log.trace("Started change password by email [{}]", email);
        contactRepository.changePasswordByEmail(myPasswordEncoder.encode(password), email);
        log.info("Password was changed by email [{}]", email);
    }

    public void verifyPassword(String password, String encodedPassword) {
        log.trace("Started verify password");

        if (!myPasswordEncoder.matches(password, encodedPassword)) {
            log.warn("Password was not verify");
            throw new PasswordsAreNotEqualException();
        }
        log.info("Password was verified");
    }

    @Transactional
    public void updateContactProfile(EditContactProfileRequestDto editContactProfileRequestDto) {
        log.trace("Started updating contact profile: [{}]", editContactProfileRequestDto);

        Contact contact = getCurrentContact();
        String oldNickname = contact.getNickname();
        String newNickname = editContactProfileRequestDto.getNickname();
        contact.setAvatarId(editContactProfileRequestDto.getAvatarId());

        contactRepository.updateNicknameInContactsAndTopics(oldNickname, newNickname);
        contactRepository.save(contact);

        log.info("Successfully updated contact and associated topics for user with email [{}]", getCurrentContact().getEmail());
    }

    public boolean isEmailExists(String email) {
        log.trace("Started check is email exists in repository");
        return contactRepository.existsByEmailIgnoreCase(email);
    }

    @Transactional
    public ContactProfileResponseDto getContactProfile() {
        Contact contact = getCurrentContact();
        log.trace("Started get contact profile by email [{}]", contact.getEmail());

        ContactProfileResponseDto responseDto = new ContactProfileResponseDto();

        responseDto.setId(contact.getId());
        responseDto.setNickname(contact.getNickname());
        responseDto.setAvatarId(contact.getAvatarId());
        responseDto.setEmail(contact.getEmail());
        responseDto.setHasPermissionSendingPrivateMessage(contact.isPermittedSendingPrivateMessage());

        log.info("Contact profile was got by email [{}]", contact.getEmail());
        return responseDto;
    }

    @Transactional
    public void permitSendingPrivateMessages() {
        Contact contact = getCurrentContact();
        log.trace("Started permit sending private messages by email [{}]", contact.getEmail());
        boolean isPermittedSendingPrivateMessage = true;

        changePermissionSendingPrivateMessages(contact.getEmail(), isPermittedSendingPrivateMessage);
        log.info("Permitted sending private messages by email [{}]", contact.getEmail());
    }

    @Transactional
    public void prohibitSendingPrivateMessages() {
        Contact contact = getCurrentContact();
        log.trace("Started prohibit sending private messages by email [{}]", contact.getEmail());
        boolean isPermittedSendingPrivateMessage = false;

        changePermissionSendingPrivateMessages(contact.getEmail(), isPermittedSendingPrivateMessage);
        log.info("Prohibited sending private messages by email [{}]", contact.getEmail());
    }

    private void changePermissionSendingPrivateMessages(String email, boolean messageByContactEmail) {
        if (!contactRepository.existsByEmailIgnoreCase(email)) {
            throw new ContactNotFoundException(
                    String.format("Contact with email [%s] is not found.", email)
            );
        }

        contactRepository.updatePermissionSendingPrivateMessageByContactEmail(email, messageByContactEmail);
    }

    @Transactional
    public void addUnreadMessageToTopicSubscribers(Contact contact, Message message) {
        List<Contact> topicSubscribers = message.getTopic().getTopicSubscribers()
                .stream()
                .filter(c -> !c.equals(contact))
                .toList();
        for (Contact topicSubscriber : topicSubscribers) {
            topicSubscriber.getUnreadMessages().add(message);
            save(topicSubscriber);
        }
    }

    @Transactional
    public void deleteUnreadMessage(Contact contact, Message message) {
        contact.getUnreadMessages().remove(message);
        save(contact);
    }

    @Transactional
    public Contact getCurrentContact() {
        try {
            Contact principal = (Contact) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return findByEmail(principal.getEmail());
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public boolean deleteUser(UUID contactId) {
        log.trace("Started delete user, contact id: [{}]", contactId);

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found"));
        log.trace("Contact: [{}]", contact);

        boolean hasTopics = topicRepository.existsByContactNickname(contact.getNickname());

        try {
            if (hasTopics) {
                log.info("User [{}] has topics, using complex delete query", contactId);
                contactRepository.markUserAsDeletedWithTopics(contactId);
            } else {
                log.info("User [{}] has no topics, using simple delete query", contactId);
                contactRepository.markUserAsDeleted(contactId);
            }
            log.info("User [{}] marked as deleted", contactId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting user [{}]: {}", contactId, e.getMessage(), e);
            return false;
        }
    }
}