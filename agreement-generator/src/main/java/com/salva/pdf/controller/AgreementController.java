package com.salva.pdf.controller;

import com.salva.pdf.dto.PdfGenerateRequest;
import com.salva.pdf.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class AgreementController {

    private final PdfGeneratorService pdfGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfGenerateRequest request) {
        try {
            log.info("Generating PDF using template: {} with {} fields",
                    request.getTemplate(), request.getFields().size());

            byte[] pdfBytes = pdfGeneratorService.generatePdf(request.getTemplate(), request.getFields());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documento.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IOException e) {
            log.error("Error generating PDF", e);
            return ResponseEntity.internalServerError()
                    .body(null);
        }
    }

    @GetMapping("/templates/{templateName}/fields")
    public ResponseEntity<String[]> listTemplateFields(@PathVariable String templateName) {
        log.info("Listing available fields for template: {}", templateName);

        if ("agreement-template.html".equals(templateName)) {
            String[] fields = {
                "tutor",
                "cpf",
                "telefone",
                "email",
                "endereco",
                "bairro",
                "cep",
                "cidade",
                "paciente",
                "especie",
                "raca",
                "sexo",
                "idade",
                "dia",
                "mes"
            };
            return ResponseEntity.ok(fields);
        }

        return ResponseEntity.notFound().build();
    }
}
