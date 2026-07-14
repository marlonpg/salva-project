package com.salva.pdf.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.Map;
import java.io.File;
import java.net.URL;

@Slf4j
@Service
public class PdfGeneratorService {

    private final Configuration freemarkerConfig;

    public PdfGeneratorService(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    public byte[] generatePdf(String templateName, Map<String, Object> data) throws IOException {
        try {
            log.info("Generating PDF using template: {} with {} fields", templateName, data.size());

            // Step 1: Process Freemarker template with data
            String html = processTemplate(templateName, data);

            // Step 2: Convert HTML to PDF using Flying Saucer
            byte[] pdfBytes = convertHtmlToPdf(html);

            log.info("PDF generated successfully, size: {} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Error generating PDF with template {}", templateName, e);
            throw new IOException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private String processTemplate(String templateName, Map<String, Object> data) throws Exception {
        log.debug("Processing Freemarker template: {}", templateName);

        Template template = freemarkerConfig.getTemplate(templateName);

        StringWriter out = new StringWriter();
        template.process(data, out);

        String html = out.toString();
        log.debug("Template processed successfully, HTML size: {} bytes", html.length());

        return html;
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {
        log.debug("Converting HTML to PDF using Flying Saucer");

        ITextRenderer renderer = new ITextRenderer();
        try {
            URL templatesUrl = this.getClass().getResource("/templates/");
            String baseUrl = templatesUrl != null ? templatesUrl.toExternalForm() : "jar:file:///templates/";
            renderer.setDocumentFromString(html, baseUrl);
        } catch (Exception e) {
            log.warn("Could not set base URL for templates, using document without base URL", e);
            renderer.setDocumentFromString(html);
        }
        renderer.layout();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        renderer.createPDF(baos);

        byte[] pdfBytes = baos.toByteArray();
        log.debug("PDF created successfully, size: {} bytes", pdfBytes.length);

        return pdfBytes;
    }
}
