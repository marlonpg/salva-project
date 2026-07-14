package com.salva.pdf.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class PdfGenerateRequest {
    private String template;
    private Map<String, Object> fields = new HashMap<>();
}
