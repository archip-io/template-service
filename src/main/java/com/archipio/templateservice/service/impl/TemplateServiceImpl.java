package com.archipio.templateservice.service.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.archipio.templateservice.dto.RenderDto;
import com.archipio.templateservice.dto.TemplateConfigDto;
import com.archipio.templateservice.dto.TemplateOutputDto;
import com.archipio.templateservice.dto.TemplateZipDto;
import com.archipio.templateservice.exception.InvalidTemplateArgumentsException;
import com.archipio.templateservice.exception.InvalidTemplateConfigurationFormatException;
import com.archipio.templateservice.exception.TemplateCodeAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateFileNotFoundException;
import com.archipio.templateservice.exception.TemplateNameAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateNotFoundException;
import com.archipio.templateservice.mapper.TemplateMapper;
import com.archipio.templateservice.persistence.entity.Parameter;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
  private final TemplateEngine customTemplateEngine;

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

  @Override
  @Transactional
  public void deleteTemplate(String code) {
    var template = templateRepository.findByCode(code).orElseThrow(TemplateNotFoundException::new);
    templateRepository.delete(template);
  }

  @Override
  public String renderTemplate(RenderDto renderDto) {
    var template =
        templateRepository
            .findByCode(renderDto.getCode())
            .orElseThrow(TemplateNotFoundException::new);

    // Check params
    var requiredParams =
        template.getParameters().stream()
            .filter(Parameter::getRequired)
            .map(Parameter::getName)
            .toList();
    var allParams = template.getParameters().stream().map(Parameter::getName).toList();
    var actualParams =
        renderDto.getParameters().stream().map(RenderDto.ParameterDto::getName).toList();

    if (new LinkedHashSet<>(actualParams).size() != actualParams.size()) {
      throw new InvalidTemplateArgumentsException("exception.invalid-template-arguments.duplicate");
    }
    if (!CollectionUtils.containsAll(actualParams, requiredParams)) {
      throw new InvalidTemplateArgumentsException(
          "exception.invalid-template-arguments.missing-required-parameters");
    }
    if (!CollectionUtils.containsAll(allParams, actualParams)) {
      throw new InvalidTemplateArgumentsException(
          "exception.invalid-template-arguments.extra-parameters");
    }

    // Find template file
    var templateFilePath = Path.of(STORAGE_PATH + "/" + template.getCode() + ".html");
    if (!Files.exists(templateFilePath)) {
      throw new TemplateFileNotFoundException();
    }

    // Render
    Context ctx = new Context();
    ctx.setVariables(
        renderDto.getParameters().stream()
            .collect(
                Collectors.toMap(
                    RenderDto.ParameterDto::getName, RenderDto.ParameterDto::getValue)));

    return customTemplateEngine.process(templateFilePath.toAbsolutePath().toString(), ctx);
  }

  @Override
  public List<TemplateOutputDto> getTemplates(Integer pageNumber, Integer pageSize) {
    if (pageNumber == null || pageNumber < 0) {
      pageNumber = 0;
    }
    if (pageSize == null || pageSize < 1) {
      pageSize = 1;
    }
    var pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));
    var page = templateRepository.findAll(pageable);
    return page.stream().map(templateMapper::toOutputDto).toList();
  }

  @Override
  public TemplateOutputDto getTemplate(String code) {
    var template = templateRepository.findByCode(code).orElseThrow(TemplateNotFoundException::new);
    return templateMapper.toOutputDto(template);
  }
}
