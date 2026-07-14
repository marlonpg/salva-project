package com.salva.pdf.config;

import freemarker.template.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FreemarkerConfig {

    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_32);
        config.setClassForTemplateLoading(this.getClass(), "/templates");
        config.setDefaultEncoding("UTF-8");
        return config;
    }
}
