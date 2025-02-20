package com.chat.yourway.listener;

import com.chat.yourway.model.Contact;
import com.chat.yourway.service.ContactOnlineService;
import com.chat.yourway.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class StompConnectionListener {
//
//    private final ContactOnlineService contactOnlineService;
//
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectEvent event) {
//        contactOnlineService.setUserOnline(getUserEmailFromEvent(event));
//        log.info("Contact [{}] is connected", getUserEmailFromEvent(event));
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        contactOnlineService.setUserOffline(getUserEmailFromEvent(event));
//        log.info("Contact [{}] is disconnected", getUserEmailFromEvent(event));
//    }
//
//    private String getUserEmailFromEvent(AbstractSubProtocolEvent event) {
//        return Objects.requireNonNull(event.getUser()).getName();
//    }
//
//}

@Component
@Slf4j
@RequiredArgsConstructor
public class StompConnectionListener {

    private final ContactOnlineService contactOnlineService;
    private final ContactService contactService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String email = getUserEmailFromEvent(event);
        if (isDeletedUser(email)) {
            log.warn("Skipping online status for deleted user: {}", email);
            return;
        }
        contactOnlineService.setUserOnline(email);
        log.info("Contact [{}] is connected", email);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String email = getUserEmailFromEvent(event);
        contactOnlineService.setUserOffline(email);
        log.info("Contact [{}] is disconnected", email);
    }

    private String getUserEmailFromEvent(AbstractSubProtocolEvent event) {
        return Objects.requireNonNull(event.getUser()).getName();
    }

    private boolean isDeletedUser(String email) {
        Contact contact = contactService.findByEmail(email);
        return contact == null || contact.isDeleted();
    }
}

