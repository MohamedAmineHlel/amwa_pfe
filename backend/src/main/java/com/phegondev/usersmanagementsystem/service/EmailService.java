package com.phegondev.usersmanagementsystem.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("nidhalnar13@gmail.com");  // Set your Gmail address as the sender
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
        };

        try {
            mailSender.send(messagePreparator);
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
}