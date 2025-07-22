package org.example.shoppingweb.controller;

import org.example.shoppingweb.DTO.ContactDTO;
import org.example.shoppingweb.entity.Contact;
import org.example.shoppingweb.repository.ContactRepository;
import org.example.shoppingweb.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;

@Controller
public class ContactController {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private EmailService emailService;


    @PostMapping("/contact")
    public String handleContact(@ModelAttribute ContactDTO contactDTO,
                                RedirectAttributes redirectAttributes) {
        Contact contact = new Contact();
        contact.setFullName(contactDTO.getFirstName().trim() + " " + contactDTO.getLastName().trim());
        contact.setEmail(contactDTO.getEmail());
        contact.setPhoneNumber(contactDTO.getPhone());
        contact.setSubject(contactDTO.getSubject());
        contact.setMessage(contactDTO.getMessage());
        contact.setStatus("Pending");
        contact.setCreatedAt(Instant.now());

        contactRepository.save(contact);
        emailService.sendContactEmail(contact);

        redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully!");
        return "redirect:/contact";
    }

}

