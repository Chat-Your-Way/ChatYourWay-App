package com.chat.yourway.integration.service.impl;

import com.chat.yourway.dto.response.TopicSubscriberResponseDto;
import com.chat.yourway.exception.NotSubscribedTopicException;
import com.chat.yourway.exception.TopicNotFoundException;
import com.chat.yourway.integration.extension.PostgresExtension;
import com.chat.yourway.integration.extension.RedisExtension;
import com.chat.yourway.model.TopicSubscriber;
import com.chat.yourway.service.interfaces.ContactService;
import com.chat.yourway.service.interfaces.TopicService;
import com.chat.yourway.service.interfaces.TopicSubscriberService;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({PostgresExtension.class, RedisExtension.class})
@SpringBootTest
@TestExecutionListeners(
    value = {
      TransactionalTestExecutionListener.class,
      DirtiesContextTestExecutionListener.class,
      DependencyInjectionTestExecutionListener.class,
      DbUnitTestExecutionListener.class,
      MockitoTestExecutionListener.class,
      ResetMocksTestExecutionListener.class
    })
public class TopicSubscriberServiceImplTest {
  @Autowired TopicService topicService;
  @Autowired ContactService contactService;
  @Autowired TopicSubscriberService topicSubscriberService;

  @Test
  @DatabaseSetup(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName("should successfully add topic to favourite when user mark topic as favourite")
  public void shouldSuccessfullyAddTopicToFavourite_whenUserMarkTopicAsFavourite() {
    // Given
    var contactEmail = "vasil1@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var expectedSize = 2;
    var topicId = 112;

    // When
    topicSubscriberService.addTopicToFavourite(topicId, contact);
    var resultList = topicService.findAllFavouriteTopics(contact);

    // Then
    assertThat(resultList.size())
        .withFailMessage("Expecting size of list of topics equals to " + expectedSize)
        .isEqualTo(expectedSize);
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName(
      "should successfully remove topic from favourite when user unmark topic as favourite")
  public void shouldSuccessfullyRemoveTopicFromFavourite_whenUserUnmarkTopicAsFavourite() {
    // Given
    var contactEmail = "vasil1@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var expectedSize = 0;
    var topicId = 111;

    // When
    topicSubscriberService.removeTopicFromFavourite(topicId, contact);
    var resultList = topicService.findAllFavouriteTopics(contact);

    // Then
    assertThat(resultList.size())
        .withFailMessage("Expecting size of list of topics equals to " + expectedSize)
        .isEqualTo(expectedSize);
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName(
      "should throw TopicNotFoundException when user mark topic as favourite and topic does not exist")
  public void shouldThrowTopicNotFoundException_whenUserMarkTopicAsFavouriteAndTopicDoesNotExist() {
    // Given
    var contactEmail = "vasil1@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 1;

    // When
    // Then
    assertThrows(
        TopicNotFoundException.class,
        () -> topicSubscriberService.addTopicToFavourite(topicId, contact));
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName(
      "should throw TopicNotFoundException when user unmark topic as favourite and topic does not exist")
  public void
      shouldThrowTopicNotFoundException_whenUserUnmarkTopicAsFavouriteAndTopicDoesNotExist() {
    // Given
    var contactEmail = "vasil1@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 1;

    // When
    // Then
    assertThrows(
        TopicNotFoundException.class,
        () -> topicSubscriberService.removeTopicFromFavourite(topicId, contact));
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName(
      "should throw NotSubscribedTopicException when user mark topic as favourite and user did not subscribe to topic")
  public void
      shouldThrowNotSubscribedTopicException_whenUserMarkTopicAsFavouriteAndUserDidNotSubscribeToTopic() {
    // Given
    var contactEmail = "vasil1@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 113;

    // When
    // Then
    assertThrows(
        NotSubscribedTopicException.class,
        () -> topicSubscriberService.addTopicToFavourite(topicId, contact));
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/favourite-topics-of-contact.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName(
      "should throw NotSubscribedTopicException when user unmark topic as favourite and user did not subscribe to topic")
  public void
      shouldThrowNotSubscribedTopicException_whenUserUnmarkTopicAsFavouriteAndUserDidNotSubscribeToTopic() {
    // Given
    var contactEmail = "vasil1@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 113;

    // When
    // Then
    assertThrows(
        NotSubscribedTopicException.class,
        () -> topicSubscriberService.removeTopicFromFavourite(topicId, contact));
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/permission-sending-message-in-private-topic.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/permission-sending-message-in-private-topic.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName(
      "should successfully permit sending private messages when user change permission of topic")
  public void shouldSuccessfullyPermitSendingPrivateMessages_whenUserChangePermissionOfTopic() {
    // Given
    var contactEmail = "vasil299@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 212;

    // When
    topicSubscriberService.permitSendingPrivateMessages(topicId, contact);

    // Then
    var result =
        topicService.findById(topicId).getTopicSubscribers().stream()
            .filter(ts -> ts.getContact().getEmail().equals(contactEmail))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected subscriber of topic is not present."));

    assertThat(result)
        .extracting(TopicSubscriberResponseDto::isPermittedSendingMessage)
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
      "should successfully prohibit sending private messages when user change permission of topic")
  public void shouldSuccessfullyProhibitSendingPrivateMessages_whenUserChangePermissionOfTopic() {
    // Given
    var contactEmail = "vasil299@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 211;

    // When
    topicSubscriberService.prohibitSendingPrivateMessages(topicId, contact);

    // Then
    var result =
        topicService.findById(topicId).getTopicSubscribers().stream()
            .filter(ts -> ts.getContact().getEmail().equals(contactEmail))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected subscriber of topic is not present."));

    assertThat(result)
        .extracting(TopicSubscriberResponseDto::isPermittedSendingMessage)
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
      "should throw TopicNotFoundException when user change permission of not existed topic")
  public void shouldThrowTopicNotFoundException_whenUserChangePermissionOfNotExistedTopic() {
    // Given
    var contactEmail = "vasil299@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 217;

    // When
    // Then
    assertThrows(
        TopicNotFoundException.class,
        () -> topicSubscriberService.permitSendingPrivateMessages(topicId, contact));
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/permission-sending-message-in-private-topic.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/permission-sending-message-in-private-topic.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName("should throw TopicNotFoundException when user change permission of public topic")
  public void shouldThrowTopicNotFoundException_whenUserChangePermissionOfPublicTopic() {
    // Given
    var contactEmail = "vasil299@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 213;

    // When
    // Then
    assertThrows(
        TopicNotFoundException.class,
        () -> topicSubscriberService.permitSendingPrivateMessages(topicId, contact));
  }

  @Test
  @DatabaseSetup(
      value = "/dataset/permission-sending-message-in-private-topic.xml",
      type = DatabaseOperation.INSERT)
  @DatabaseTearDown(
      value = "/dataset/permission-sending-message-in-private-topic.xml",
      type = DatabaseOperation.DELETE)
  @DisplayName("should throw NotSubscribedException when user change permission of topic")
  public void shouldThrowNotSubscribedException_whenUserChangePermissionOfTopic() {
    // Given
    var contactEmail = "anton912@gmail.com";
    var contact = contactService.findByEmail(contactEmail);
    var topicId = 212;

    // When
    // Then
    assertThrows(
        NotSubscribedTopicException.class,
        () -> topicSubscriberService.permitSendingPrivateMessages(topicId, contact));
  }
}
