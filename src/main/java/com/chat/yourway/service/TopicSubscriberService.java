package com.chat.yourway.service;

import com.chat.yourway.dto.response.ContactResponseDto;
import com.chat.yourway.exception.ContactAlreadySubscribedToTopicException;
import com.chat.yourway.exception.NotSubscribedTopicException;
import com.chat.yourway.exception.TopicNotFoundException;
import com.chat.yourway.exception.TopicSubscriberNotFoundException;
import com.chat.yourway.model.Contact;
import com.chat.yourway.model.Topic;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface TopicSubscriberService {

  /**
   * Subscribes a contact to a topic with the specified email and topic ID.
   *
   * @param email   Contact's email.
   * @param topicId Topic ID.
   * @throws ContactAlreadySubscribedToTopicException if the contact is already subscribed to the
   *                                                  topic.
   */
  void subscribeToTopicById(String email, UUID topicId);

  /**
   * Unsubscribes a contact from a topic with the specified email and topic ID.
   *
   * @param email   Contact's email.
   * @param topicId Topic ID.
   * @throws TopicSubscriberNotFoundException if the contact was not subscribed to the topic.
   */
  void unsubscribeFromTopicById(String email, UUID topicId);

  /**
   * Retrieves a list of contacts who are subscribers to the topic by ID.
   *
   * @param id The ID of the topic.
   * @return A list of contacts who are subscribers to the topic.
   */
  List<ContactResponseDto> findAllSubscribersByTopicId(UUID id);

  /**
   * Checks if a contact is subscribed to a topic with the specified email and topic ID.
   *
   * @return true if the contact is subscribed to the topic.
   */
  boolean hasContactSubscribedToTopic(Topic topic, Contact contact);

  /**
   * Adds the specified topic to the user's list of favorite topics.
   *
   * @param topicId     The ID of the topic to be added to favorites.
   * @param userDetails The details of the user for whom the topic is to be added to favorites.
   * @throws TopicNotFoundException      If topic does not exist.
   * @throws NotSubscribedTopicException If contact does not subscribed to topic.
   */
  void addTopicToFavourite(UUID topicId, UserDetails userDetails);

  /**
   * Removes the specified topic from the user's list of favorite topics.
   *
   * @param topicId     The ID of the topic to be removed from favorites.
   * @param userDetails The details of the user for whom the topic is to be removed from favorites.
   * @throws TopicNotFoundException      If topic does not exist.
   * @throws NotSubscribedTopicException If contact does not subscribed to topic.
   */
  void removeTopicFromFavourite(UUID topicId, UserDetails userDetails);


  /**
   * Registers a complaint for a specific topic.
   *
   * This method allows a user to complain about a particular topic identified by its unique identifier.
   * The complaint details, such as the user's information, will be recorded for further investigation.
   *
   * @param topicId      The unique identifier of the topic being complained about.
   * @param userDetails  The details of the user lodging the complaint.
   *                     This should include relevant information like user ID, username, etc.
   *                     Ensure that the userDetails parameter is not null.
   *
   * @throws TopicNotFoundException      If topic does not exist.
   * @throws NotSubscribedTopicException If contact does not subscribed to topic.
   */
  void complainTopic(UUID topicId, UserDetails userDetails);
}