package com.chat.yourway.repository.redis;

import com.chat.yourway.model.event.ContactEvent;
import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactEventRedisRepository extends CrudRepository<ContactEvent, String> {

  List<ContactEvent> findAllByEmail(String email);

  List<ContactEvent> findAllByTopicId(UUID topicId);

}