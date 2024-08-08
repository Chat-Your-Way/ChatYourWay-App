package com.chat.yourway.security;

import com.chat.yourway.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final ContactService contactService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return contactService.findByEmail(username);
    }
}
