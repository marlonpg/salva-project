package com.salva.project.backend.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendAccessCode(String toEmail, String code) {
		logger.info("📧 Access Code for {}: {}", toEmail, code);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(toEmail);
			message.setSubject("Your Access Code");
			message.setText("Your access code is: " + code + "\n\nValid for 5 minutes.");
			mailSender.send(message);
		} catch (Exception e) {
			logger.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
		}
	}
}
