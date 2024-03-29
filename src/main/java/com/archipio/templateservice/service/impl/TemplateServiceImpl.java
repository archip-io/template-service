package com.archipio.templateservice.service.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.archipio.templateservice.dto.TemplateConfigDto;
import com.archipio.templateservice.dto.TemplateZipDto;
import com.archipio.templateservice.exception.InvalidTemplateConfigurationFormatException;
import com.archipio.templateservice.exception.TemplateCodeAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateNameAlreadyExistsException;
import com.archipio.templateservice.mapper.TemplateMapper;
import com.archipio.templateservice.persistence.repository.TemplateRepository;
import com.archipio.templateservice.service.TemplateService;
import com.archipio.templateservice.service.ZipService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

  private static final String STORAGE_PATH = "./storage";

  static {
    try {
      Files.createDirectories(Path.of(STORAGE_PATH));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final ZipService zipService;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final TemplateRepository templateRepository;
  private final TemplateMapper templateMapper;

  @Override
  @Transactional
  public void importTemplate(MultipartFile data) {
    // Extract files from zip archive
    TemplateZipDto templateZipDto;
    try {
      var file = File.createTempFile("data.zip", "");
      FileUtils.copyInputStreamToFile(data.getInputStream(), file);
      templateZipDto = zipService.extractAll(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Validate JSON format
    TemplateConfigDto templateConfigDto;
    try {
      templateConfigDto =
          objectMapper.readValue(templateZipDto.getJsonConfig(), TemplateConfigDto.class);
    } catch (JsonProcessingException e) {
      throw new InvalidTemplateConfigurationFormatException();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Validate data in JSON
    var violations = validator.validate(templateConfigDto);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }

    if (templateRepository.existsByCode(templateConfigDto.getCode())) {
      throw new TemplateCodeAlreadyExistsException();
    }
    if (templateRepository.existsByName(templateConfigDto.getName())) {
      throw new TemplateNameAlreadyExistsException();
    }

    // Save template file
    try {
      Files.copy(
          templateZipDto.getHtmlTemplate().toPath(),
          Path.of(STORAGE_PATH, templateConfigDto.getCode() + ".html"),
          REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Save template configuration
    var template = templateMapper.toEntity(templateConfigDto);
    templateRepository.save(template);
  }
}
