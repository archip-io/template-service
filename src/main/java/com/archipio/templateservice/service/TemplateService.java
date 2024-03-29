package com.archipio.templateservice.service;

import com.archipio.templateservice.dto.RenderDto;
import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {

  void importTemplate(MultipartFile data);

    void deleteTemplate(String code);

  String renderTemplate(RenderDto renderDto);
}
