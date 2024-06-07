package com.chat.yourway.service.impl;

import com.chat.yourway.dto.response.ContactResponseDto;
import com.chat.yourway.mapper.ContactMapper;
import com.chat.yourway.model.Contact;
import com.chat.yourway.model.redis.ContactOnline;
import com.chat.yourway.repository.redis.ContactOnlineRedisRepository;
import com.chat.yourway.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactOnlineService {

    private final ContactOnlineRedisRepository contactOnlineRedisRepository;
    private final ContactService contactService;
    private final ContactMapper contactMapper;

    public void setUserOnline(String contactEmail) {
        setUserOnline(contactEmail, null);
    }

    public void setUserOnline(String contactEmail, UUID topicId) {
        ContactOnline contactOnline = ContactOnline.builder()
                .id(contactEmail)
                .timestamp(LocalDateTime.now())
                .topicId(topicId)
                .build();
        contactOnlineRedisRepository.save(contactOnline);
        //TODO відправити нотифікацію всім юзерам які онлайн в топіку що змінився склад юзерів
        //щоб фронт міг завантажити оновлений список юзерів
    }

    public void setUserOffline(String contactEmail) {
        //TODO відправити нотифікацію всім юзерам які онлайн в топіку що змінився склад юзерів
        //щоб фронт міг завантажити оновлений список юзерів
        contactOnlineRedisRepository.deleteById(contactEmail);
    }

    public List<ContactResponseDto> getOnlineUsersByTopicId(UUID topicId) {
        //TODO додати перевірку на приватний топік. якщо це id чужого прив.топіку, то ексепшен
        List<Contact> result = new ArrayList<>();
        List<ContactOnline> contactOnlines = contactOnlineRedisRepository.findAllByTopicId(topicId);
        for (ContactOnline contactOnline : contactOnlines) {
            result.add(contactService.findByEmail(contactOnline.getId()));
        }
        return contactMapper.toListResponseDto(result);
    }

    public ContactOnline getContactOnline(String contactEmail) {
        return contactOnlineRedisRepository.findById(contactEmail).orElse(null);
    }

    public ContactOnline save(ContactOnline contactOnline) {
        return contactOnlineRedisRepository.save(contactOnline);
    }

    public List<ContactResponseDto> getOnlineUsers() {
        List<Contact> result = new ArrayList<>();
        Iterable<ContactOnline> all = contactOnlineRedisRepository.findAll();
        for (ContactOnline contactOnline : all) {
            result.add(contactService.findByEmail(contactOnline.getId()));
        }
        return contactMapper.toListResponseDto(result);
    }
}
