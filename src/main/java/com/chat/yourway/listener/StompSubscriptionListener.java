package com.chat.yourway.listener;

import com.chat.yourway.config.websocket.WebSocketProperties;
import com.chat.yourway.model.Contact;
import com.chat.yourway.service.ContactOnlineService;
import com.chat.yourway.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chat.yourway.utils.Constants.SLASH;
import static com.chat.yourway.utils.Constants.UUID_REGEX_PATTERN;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompSubscriptionListener {

    private final WebSocketProperties webSocketProperties;
    private final ContactOnlineService contactOnlineService;
    private final ContactService contactService;

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        String destination = getDestination(event);
        String email = getEmail(event);

        if (isDeletedUser(email)) {
            log.warn("Skipping subscription handling for deleted user: {}", email);
            return;
        }

        if (isTopicDestination(destination)) {
            UUID topicId = getTopicId(event);
            contactOnlineService.setUserOnline(email, topicId);
            log.info("Contact [{}] opened topic [{}]", email, destination);
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        String destination = getDestination(event);
        String email = getEmail(event);

        if (isDeletedUser(email)) {
            log.warn("Skipping unsubscription handling for deleted user: {}", email);
            return;
        }

        if (isTopicDestination(destination)) {
            contactOnlineService.setUserOffline(email);
            log.info("Contact [{}] unsubscribed from [{}]", email, destination);
        }
    }

    private String getEmail(AbstractSubProtocolEvent event) {
        return Objects.requireNonNull(event.getUser()).getName();
    }

    private String getDestination(AbstractSubProtocolEvent event) {
        return SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination();
    }

    private UUID getTopicId(AbstractSubProtocolEvent event) {
        String destination = getDestination(event);
        Pattern pattern = Pattern.compile(UUID_REGEX_PATTERN);
        Matcher matcher = pattern.matcher(destination);

        if (matcher.find()) {
            return UUID.fromString(matcher.group());
        }
        return null;
    }

    private String getTopicDestination() {
        return webSocketProperties.getTopicPrefix() + SLASH;
    }

    private boolean isTopicDestination(String destination) {
        return destination != null && destination.startsWith(getTopicDestination());
    }

    private boolean isDeletedUser(String email) {
        Contact contact = contactService.findByEmail(email);
        return contact == null || contact.isDeleted();
    }
}
