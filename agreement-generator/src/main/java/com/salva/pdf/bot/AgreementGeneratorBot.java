package com.salva.pdf.bot;

import com.salva.pdf.service.TelegramAgreementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;

@Slf4j
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
public class AgreementGeneratorBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final TelegramAgreementService agreementService;

    public AgreementGeneratorBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            TelegramAgreementService agreementService) {
        super(botToken);
        this.botUsername = botUsername;
        this.agreementService = agreementService;
        log.info("AgreementGeneratorBot initialized as @{}", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Message message = update.getMessage();
        String text = message.getText().trim();
        Long chatId = message.getChatId();

        log.info("Message from chat {}: '{}'", chatId, text);

        if (text.startsWith("/agreement")) {
            handleAgreementCommand(chatId, text);
        } else if (text.equals("/start")) {
            sendReply(chatId, "Welcome to Agreement Generator Bot! 📄\n\n" +
                    "Use /agreement to generate an agreement PDF.\n\n" +
                    "Example: /agreement template=agreement-template-v2.html&tutor=John Doe&paciente=Fluffy");
        } else if (text.equals("/help")) {
            sendReply(chatId, "Available commands:\n\n" +
                    "/start - Show welcome message\n" +
                    "/agreement - Generate an agreement PDF\n" +
                    "/help - Show this help message\n\n" +
                    "For /agreement, provide parameters as key=value pairs separated by &");
        }
    }

    private void handleAgreementCommand(Long chatId, String text) {
        try {
            sendReply(chatId, "⏳ Generating your agreement PDF...");

            byte[] pdfBytes = agreementService.generateAgreement(text);

            if (pdfBytes != null && pdfBytes.length > 0) {
                sendDocument(chatId, pdfBytes, "agreement.pdf");
            } else {
                sendReply(chatId, "❌ Failed to generate PDF. Please check your parameters.");
            }
        } catch (Exception e) {
            log.error("Error handling agreement command: {}", e.getMessage(), e);
            sendReply(chatId, "❌ Error: " + e.getMessage());
        }
    }

    public void sendReply(Long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage());
        }
    }

    public void sendDocument(Long chatId, byte[] pdfBytes, String filename) {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(new ByteArrayInputStream(pdfBytes), filename))
                .build();
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Failed to send document to chat {}: {}", chatId, e.getMessage());
        }
    }
}
