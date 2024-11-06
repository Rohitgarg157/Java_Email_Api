package com.baeldung.registration.listener;

import java.io.File;
import org.springframework.core.io.ClassPathResource;

import java.util.Locale;
import java.util.UUID;

import com.baeldung.service.IUserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.baeldung.persistence.model.User;
import com.baeldung.registration.OnRegistrationCompleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    private IUserService service;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    // API

    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        try {
			this.confirmRegistration(event);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void confirmRegistration(final OnRegistrationCompleteEvent event) throws MessagingException {
        final User user = event.getUser();
        final String token = UUID.randomUUID().toString();
        service.createVerificationTokenForUser(user, token);

        final MimeMessage email = constructEmailMessageWithAttachment(event, user, token);
        mailSender.send(email);
    }

   

    private MimeMessage constructEmailMessageWithAttachment(final OnRegistrationCompleteEvent event, final User user, final String token) throws MessagingException {
        final String recipientAddress = user.getEmail();
        final String subject = "Registration Confirmation";
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm?token=" + token;
      //  final String message = messages.getMessage("message.regSuccLink", null, "You registered successfully. To confirm your registration, please click on the below link.", event.getLocale());
        
        
        final String message = messages.getMessage("message.regSuccLink",new Object[]{user.getName(), confirmationUrl}, 
                Locale.ENGLISH);

       
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // Create MimeMessageHelper and attach files
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); 
        helper.setTo(recipientAddress);
        helper.setSubject(subject);
        helper.setText(message + " \r\n" + confirmationUrl);
        helper.setFrom(env.getProperty("support.email"));

        helper.addAttachment("Logo1.png", new ClassPathResource("image2.png"));
        helper.addAttachment("Logo2.png", new ClassPathResource("image3.jpeg"));
       return mimeMessage;
    }    

}
