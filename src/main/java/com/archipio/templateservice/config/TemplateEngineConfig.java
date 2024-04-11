package com.archipio.templateservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration
@RequiredArgsConstructor
public class TemplateEngineConfig {

  private final TemplateEngine templateEngine;

  @Bean
  public TemplateEngine customTemplateEngine() {
    var templateResolver = new FileTemplateResolver();
    templateResolver.setPrefix("");
    templateResolver.setTemplateMode("HTML");
    templateEngine.setTemplateResolver(templateResolver);

    return templateEngine;
  }
}
