package com.salva.project.backend.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender mailSender;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendAccessCode(String toEmail, String code) {
		logger.info("📧 Access Code for {}: {}", toEmail, code);

		String encodedEmail = URLEncoder.encode(toEmail, StandardCharsets.UTF_8);
		String magicLink = frontendUrl + "?verifyEmail=" + encodedEmail + "&code=" + code;

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(toEmail);
			message.setSubject("Seu acesso foi aprovado");
			message.setText(
				"Seu acesso ao sistema foi aprovado!\n\n" +
				"Clique no link abaixo para entrar:\n" +
				magicLink + "\n\n" +
				"Ou insira o código manualmente: " + code + "\n\n" +
				"O link expira em 5 minutos."
			);
			mailSender.send(message);
		} catch (Exception e) {
			logger.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
		}
	}
}
