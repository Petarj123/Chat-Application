package com.auth.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * The type Email sender service.
 */
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    /**
     * Sends a registration confirmation email to the specified email address.
     *
     * @param toEmail the email address to send the registration confirmation to
     */
    public void sendRegistrationEmail(String toEmail){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nnotificationovic@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Registration Confirmation");
        message.setText("Dear "+toEmail + ",\n" +
                "\n" +
                "Thank you for registering with our service! We're excited to have you as a member of our community.\n" +
                "\n" +
                "If you have any questions or need assistance with your account, please do not hesitate to contact us at [support email address].\n" +
                "\n" +
                "Best regards,");
        mailSender.send(message);
    }

    /**
     * Sends a password recovery email to the specified email address, containing a reset link.
     *
     * @param to        the email address to send the recovery email to
     * @param resetLink the reset link to include in the email
     */
    public void sendRecoveryPasswordEmail(String to, String resetLink){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nnotificationovic@gmail.com");
        message.setTo(to);
        message.setSubject("Password Recovery");
        message.setText("Please click on following link "+resetLink);
        mailSender.send(message);
    }
}
