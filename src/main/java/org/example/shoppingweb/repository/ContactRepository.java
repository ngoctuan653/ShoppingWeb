package org.example.shoppingweb.repository;

import org.example.shoppingweb.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
}
