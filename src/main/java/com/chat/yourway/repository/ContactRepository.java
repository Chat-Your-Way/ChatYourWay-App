package com.chat.yourway.repository;

import com.chat.yourway.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * {@link ContactRepository}
 *
 * @author Dmytro Trotsenko on 7/26/23
 */
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    Optional<Contact> findByEmail(String email);
    Optional<Contact> findByUsername(String username);

    @Modifying
    @Query("UPDATE Contact c set c.password = :password where c.username = :username")
    void changePasswordByUsername(String password, String username);


}
