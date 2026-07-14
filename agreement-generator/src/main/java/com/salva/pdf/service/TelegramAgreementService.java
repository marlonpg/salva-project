package com.salva.pdf.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramAgreementService {

    private final PdfGeneratorService pdfGeneratorService;

    public byte[] generateAgreement(String command) throws Exception {
        Map<String, Object> params = parseCommand(command);

        if (!params.containsKey("template")) {
            throw new IllegalArgumentException("Missing 'template' parameter. Example: /agreement template=agreement-template-v2.html&tutor=John");
        }

        String templateName = (String) params.remove("template");

        log.info("Generating agreement with template: {} and {} fields", templateName, params.size());

        return pdfGeneratorService.generatePdf(templateName, params);
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
}
