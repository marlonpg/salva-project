package com.salva.pdf.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TemplateRegistry {

    private final Map<String, String> templateMap = new HashMap<>();
    private final Map<String, String> descriptionMap = new HashMap<>();

    public TemplateRegistry() {
        // Map document names to template files
        templateMap.put("termo 1", "agreement-template-v2.html");
        descriptionMap.put("termo 1", "Termo de Autorização de Transporte Veterinário");

        log.info("TemplateRegistry initialized with {} templates", templateMap.size());
    }

    public String getTemplate(String termName) {
        return templateMap.get(termName.toLowerCase().trim());
    }

    public String getDescription(String termName) {
        return descriptionMap.getOrDefault(termName.toLowerCase().trim(), "");
    }

    public boolean hasTemplate(String termName) {
        return templateMap.containsKey(termName.toLowerCase().trim());
    }

    public Map<String, String> getAllTemplates() {
        return new HashMap<>(templateMap);
    }

    public String getFormattedTemplateList() {
        StringBuilder sb = new StringBuilder("📋 Documentos disponíveis:\n\n");
        int index = 1;
        for (Map.Entry<String, String> entry : templateMap.entrySet()) {
            sb.append(index).append(". ").append(entry.getKey()).append(" - ").append(descriptionMap.get(entry.getKey())).append("\n");
            index++;
        }
        sb.append("\nPara selecionar, digite: \"Quero gerar [nome do documento]\"");
        return sb.toString();
    }
}
