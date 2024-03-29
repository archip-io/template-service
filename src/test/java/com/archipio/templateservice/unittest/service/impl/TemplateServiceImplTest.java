package com.archipio.templateservice.unittest.service.impl;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import com.archipio.templateservice.dto.TemplateConfigDto;
import com.archipio.templateservice.exception.InvalidTemplateConfigurationFormatException;
import com.archipio.templateservice.exception.TemplateCodeAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateNameAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateNotFoundException;
import com.archipio.templateservice.mapper.TemplateMapper;
import com.archipio.templateservice.persistence.entity.Template;
import com.archipio.templateservice.persistence.repository.TemplateRepository;
import com.archipio.templateservice.service.impl.TemplateServiceImpl;
import com.archipio.templateservice.service.impl.ZipServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class TemplateServiceImplTest {

  private static final String RESOURCES_DIR_PATH =
      Paths.get("src", "test", "resources").toFile().getAbsolutePath();

  @Spy private ZipServiceImpl zipService = new ZipServiceImpl();
  @Mock private ObjectMapper objectMapper;
  @Mock private Validator validator;
  @Mock private TemplateRepository templateRepository;
  @Mock private TemplateMapper templateMapper;

  @InjectMocks private TemplateServiceImpl templateService;

  @Test
  void importTemplate_whenMultipartFileContainsValidZip_thenSaveFileAndConfig() throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    final var templateCode = "test";
    final var templateName = "Test";
    final var templateConfigDto =
        TemplateConfigDto.builder().name(templateName).code(templateCode).build();
    final var template = new Template();
    when(objectMapper.readValue(any(File.class), eq(TemplateConfigDto.class)))
        .thenReturn(templateConfigDto);
    when(validator.validate(templateConfigDto)).thenReturn(Set.of());
    when(templateRepository.existsByCode(templateCode)).thenReturn(false);
    when(templateRepository.existsByName(templateName)).thenReturn(false);
    when(templateMapper.toEntity(templateConfigDto)).thenReturn(template);
    when(templateRepository.save(template)).thenReturn(template);

    // Do
    templateService.importTemplate(multipartFile);

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
    verify(objectMapper, times(1)).readValue(any(File.class), eq(TemplateConfigDto.class));
    verify(validator, times(1)).validate(templateConfigDto);
    verify(templateRepository, times(1)).existsByCode(templateCode);
    verify(templateRepository, times(1)).existsByName(templateName);
    verify(templateMapper, times(1)).toEntity(templateConfigDto);
    verify(templateRepository, times(1)).save(template);
  }

  @Test
  void importTemplate_whenZipServiceThrownIOException_thenThrownRuntimeException()
      throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    doThrow(IOException.class).when(zipService).extractAll(any(File.class));

    // Do
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> templateService.importTemplate(multipartFile))
        .withCauseExactlyInstanceOf(IOException.class);

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
  }

  @Test
  void
      importTemplate_whenTemplateConfigurationIsInvalid_thenThrownInvalidTemplateConfigurationFormatException()
          throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    doThrow(JsonProcessingException.class)
        .when(objectMapper)
        .readValue(any(File.class), eq(TemplateConfigDto.class));

    // Do
    assertThatExceptionOfType(InvalidTemplateConfigurationFormatException.class)
        .isThrownBy(() -> templateService.importTemplate(multipartFile));

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
    verify(objectMapper, times(1)).readValue(any(File.class), eq(TemplateConfigDto.class));
  }

  @Test
  void importTemplate_whenObjectMapperThrownIOException_thenThrownRuntimeException()
      throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    when(objectMapper.readValue(any(File.class), eq(TemplateConfigDto.class)))
        .thenThrow(IOException.class);

    // Do
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> templateService.importTemplate(multipartFile))
        .withCauseExactlyInstanceOf(IOException.class);

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
    verify(objectMapper, times(1)).readValue(any(File.class), eq(TemplateConfigDto.class));
  }

  @Test
  void importTemplate_whenTemplateConfigIsInvalid_thenThrownConstraintViolationException()
      throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    final var templateConfigDto = TemplateConfigDto.builder().build();
    when(objectMapper.readValue(any(File.class), eq(TemplateConfigDto.class)))
        .thenReturn(templateConfigDto);
    when(validator.validate(templateConfigDto)).thenReturn(Set.of(mock(ConstraintViolation.class)));

    // Do
    assertThatExceptionOfType(ConstraintViolationException.class)
        .isThrownBy(() -> templateService.importTemplate(multipartFile));

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
    verify(objectMapper, times(1)).readValue(any(File.class), eq(TemplateConfigDto.class));
    verify(validator, times(1)).validate(templateConfigDto);
  }

  @Test
  void importTemplate_whenTemplateCodeAlreadyExists_thenThrownTemplateCodeAlreadyExistsException()
      throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    final var templateCode = "test";
    final var templateConfigDto = TemplateConfigDto.builder().code(templateCode).build();
    when(objectMapper.readValue(any(File.class), eq(TemplateConfigDto.class)))
        .thenReturn(templateConfigDto);
    when(validator.validate(templateConfigDto)).thenReturn(Set.of());
    when(templateRepository.existsByCode(templateCode)).thenReturn(true);

    // Do
    assertThatExceptionOfType(TemplateCodeAlreadyExistsException.class)
        .isThrownBy(() -> templateService.importTemplate(multipartFile));

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
    verify(objectMapper, times(1)).readValue(any(File.class), eq(TemplateConfigDto.class));
    verify(validator, times(1)).validate(templateConfigDto);
    verify(templateRepository, times(1)).existsByCode(templateCode);
  }

  @Test
  void importTemplate_whenTemplateNameAlreadyExists_thenThrownTemplateNameAlreadyExistsException()
      throws IOException {
    // Prepare
    final var multipartFile =
        new MockMultipartFile(
            "data",
            "data.zip",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            new FileInputStream(RESOURCES_DIR_PATH + "/valid.zip"));
    final var templateCode = "test";
    final var templateName = "Test";
    final var templateConfigDto =
        TemplateConfigDto.builder().name(templateName).code(templateCode).build();
    when(objectMapper.readValue(any(File.class), eq(TemplateConfigDto.class)))
        .thenReturn(templateConfigDto);
    when(validator.validate(templateConfigDto)).thenReturn(Set.of());
    when(templateRepository.existsByCode(templateCode)).thenReturn(false);
    when(templateRepository.existsByName(templateName)).thenReturn(true);

    // Do
    assertThatExceptionOfType(TemplateNameAlreadyExistsException.class)
        .isThrownBy(() -> templateService.importTemplate(multipartFile));

    // Check
    verify(zipService, times(1)).extractAll(any(File.class));
    verify(objectMapper, times(1)).readValue(any(File.class), eq(TemplateConfigDto.class));
    verify(validator, times(1)).validate(templateConfigDto);
    verify(templateRepository, times(1)).existsByCode(templateCode);
    verify(templateRepository, times(1)).existsByName(templateName);
  }

  @Test
  public void deleteTemplate_whenTemplateExists_thenDeleteTemplate() {
    // Prepare
    final var code = "code";
    final var template = new Template();

    when(templateRepository.findByCode(code)).thenReturn(Optional.of(template));
    doNothing().when(templateRepository).delete(template);

    // Do
    templateService.deleteTemplate(code);

    // Check
    verify(templateRepository, times(1)).findByCode(code);
    verify(templateRepository, times(1)).delete(template);
  }

  @Test
  public void deleteTemplate_whenTemplateNotExists_thenThrownTemplateNotFoundException() {
    // Prepare
    final var code = "code";

    when(templateRepository.findByCode(code)).thenReturn(Optional.empty());

    // Do
    assertThatExceptionOfType(TemplateNotFoundException.class).isThrownBy(() -> templateService.deleteTemplate(code));

    // Check
    verify(templateRepository, times(1)).findByCode(code);
  }
}
