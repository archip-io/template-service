package com.archipio.templateservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {

  void importTemplate(MultipartFile data);

    void deleteTemplate(String code);
}
