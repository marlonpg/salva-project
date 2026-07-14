package com.salva.pdf.service;

import com.salva.pdf.bot.TemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramAgreementService {

    private final PdfGeneratorService pdfGeneratorService;
    private final TemplateRegistry templateRegistry;

    public byte[] generateAgreement(String command) throws Exception {
        Map<String, Object> params = parseCommand(command);

        if (!params.containsKey("template")) {
            throw new IllegalArgumentException("Missing 'template' parameter. Example: /agreement template=agreement-template-v2.html&tutor=John");
        }

        String templateName = (String) params.remove("template");

        log.info("Generating agreement with template: {} and {} fields", templateName, params.size());

        return pdfGeneratorService.generatePdf(templateName, params);
    }

    public Optional<byte[]> generateFromFieldList(String userMessage) {
        try {
            Map<String, Object> fields = parseFieldList(userMessage);

            if (fields.isEmpty()) {
                return Optional.empty();
            }

            String templateName = templateRegistry.getTemplate("termo 1");

            log.info("Generating agreement from field list with {} fields", fields.size());
            byte[] pdfBytes = pdfGeneratorService.generatePdf(templateName, fields);

            return Optional.of(pdfBytes);
        } catch (Exception e) {
            log.error("Error generating agreement from field list: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<String> getFieldsForTemplate(String userMessage) {
        String pattern = "quero gerar";
        String lowerMessage = userMessage.toLowerCase().trim();

        if (!lowerMessage.contains(pattern)) {
            return Optional.empty();
        }

        String termName = lowerMessage.replace(pattern, "").trim();
        if (termName.isEmpty()) {
            return Optional.empty();
        }

        if (!templateRegistry.hasTemplate(termName)) {
            return Optional.empty();
        }

        String templateName = templateRegistry.getTemplate(termName);

        try {
            String fields = formatFieldsForDisplay(templateName);
            return Optional.of(fields);
        } catch (Exception e) {
            log.error("Error getting fields for template: {}", templateName, e);
            return Optional.of("Erro ao buscar campos do formulário. Tente novamente mais tarde.");
        }
    }

    private String formatFieldsForDisplay(String templateName) throws Exception {
        if (templateName.equals("agreement-template.html")) {
            return "Campos necessários:\n" +
                   "1. tutor - Nome do tutor\n" +
                   "2. cpf - CPF do tutor\n" +
                   "3. telefone - Telefone de contato\n" +
                   "4. email - Email\n" +
                   "5. endereco - Endereço\n" +
                   "6. bairro - Bairro\n" +
                   "7. cep - CEP\n" +
                   "8. cidade - Cidade\n" +
                   "9. paciente - Nome do paciente (animal)\n" +
                   "10. especie - Espécie\n" +
                   "11. raca - Raça\n" +
                   "12. sexo - Sexo\n" +
                   "13. idade - Idade\n" +
                   "14. dia - Dia do documento\n" +
                   "15. mes - Mês do documento";
        }

        return "Campos disponíveis: tutor, cpf, telefone, email, endereco, bairro, cep, cidade, paciente, especie, raca, sexo, idade, dia, mes";
    }

    private Map<String, Object> parseCommand(String command) {
        Map<String, Object> params = new HashMap<>();

        String[] parts = command.split("\\s+", 2);
        if (parts.length < 2) {
            return params;
        }

        String paramString = parts[1];
        String[] paramPairs = paramString.split("&");

        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                params.put(key, value);
            }
        }

        log.debug("Parsed parameters: {}", params.keySet());
        return params;
    }

    private Map<String, Object> parseFieldList(String userMessage) {
        Map<String, Object> fields = new HashMap<>();

        // Pattern: number. fieldname - value
        Pattern pattern = Pattern.compile("^\\d+\\.\\s*([a-zA-Z0-9_]+)\\s*-\\s*(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(userMessage);

        while (matcher.find()) {
            String fieldName = matcher.group(1).trim();
            String fieldValue = matcher.group(2).trim();

            // Only add non-empty values
            if (!fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
                log.debug("Parsed field: {} = {}", fieldName, fieldValue);
            }
        }

        return fields;
    }
}
