package com.archipio.templateservice.service;

import com.archipio.templateservice.dto.RenderDto;
import com.archipio.templateservice.dto.TemplateOutputDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {

  void importTemplate(MultipartFile data);

  String renderTemplate(RenderDto renderDto);

  List<TemplateOutputDto> getTemplates(Integer pageNumber, Integer pageSize);

  TemplateOutputDto getTemplate(String code);

  void deleteTemplate(String code);
}
