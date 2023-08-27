package com.auth.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    
    public void sendRegistrationEmail(String toEmail){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pjankovic03@gmail.com");
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

    
    public void sendRecoveryPasswordEmail(String to, String resetLink){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pjankovic03@gmail.com");
        message.setTo(to);
        message.setSubject("Password Recovery");
        message.setText("Please click on following link " + buildResetLink(resetLink));
        mailSender.send(message);
    }
    private String buildResetLink(String resetToken) {
        String resetUrl = "http://localhost:50981/#/reset-password"; // Specify the reset password URL
        return resetUrl + "?token=" + resetToken;
    }
}
