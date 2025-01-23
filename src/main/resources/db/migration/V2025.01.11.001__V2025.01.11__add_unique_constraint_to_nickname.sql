ALTER TABLE chat.contacts
    ADD CONSTRAINT unique_nickname UNIQUE (nickname);