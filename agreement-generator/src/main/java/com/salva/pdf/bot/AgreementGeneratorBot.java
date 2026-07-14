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
            sendReply(chatId, "Bem-vindo ao Agreement Generator Bot! 📄\n\n" +
                    "Você pode:\n" +
                    "• Digitar 'Quero gerar termo 1' para ver os campos necessários\n" +
                    "• Colar os dados no formato listado para gerar o PDF automaticamente\n" +
                    "• Usar /agreement para gerar com comando\n" +
                    "• Digitar /help para mais opções");
        } else if (text.equals("/help")) {
            sendReply(chatId, "📖 Como usar:\n\n" +
                    "1️⃣ Digitar: 'Quero gerar termo 1'\n" +
                    "2️⃣ Copiar e colar os dados (1. tutor - valor, 2. cpf - valor, ...)\n" +
                    "3️⃣ Bot gerará e enviará o PDF automaticamente\n\n" +
                    "Ou use /agreement para comando manual");
        } else if (text.toLowerCase().contains("quero gerar")) {
            handleTemplateFieldsRequest(chatId, text);
        } else if (isFieldListFormat(text)) {
            handleFieldListRequest(chatId, text);
        }
    }

    private void handleTemplateFieldsRequest(Long chatId, String text) {
        var fieldsInfo = agreementService.getFieldsForTemplate(text);
        if (fieldsInfo.isPresent()) {
            sendReply(chatId, fieldsInfo.get());
        } else {
            sendReply(chatId, "❓ Desculpe, não entendi qual documento você quer gerar.\n\n" +
                    "Tente: 'Quero gerar termo 1'");
        }
    }

    private boolean isFieldListFormat(String text) {
        // Check if message contains numbered fields like "1. fieldname - value"
        boolean isFieldList = text.matches("(?s)^\\s*\\d+\\..*-.*");
        log.debug("isFieldListFormat check: {} for text: {}", isFieldList, text.substring(0, Math.min(50, text.length())));
        return isFieldList;
    }

    private void handleFieldListRequest(Long chatId, String text) {
        log.info("Detected field list format, processing...");
        try {
            sendReply(chatId, "⏳ Processando dados e gerando PDF...");

            var pdfBytes = agreementService.generateFromFieldList(text);

            if (pdfBytes.isPresent()) {
                log.info("PDF generated successfully, sending to chat {}", chatId);
                sendDocument(chatId, pdfBytes.get(), "termo-autorizacao.pdf");
            } else {
                log.warn("Failed to generate PDF from field list");
                sendReply(chatId, "❌ Não consegui extrair os dados. Verifique o formato e tente novamente.");
            }
        } catch (Exception e) {
            log.error("Error handling field list request: {}", e.getMessage(), e);
            sendReply(chatId, "❌ Erro ao processar: " + e.getMessage());
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
