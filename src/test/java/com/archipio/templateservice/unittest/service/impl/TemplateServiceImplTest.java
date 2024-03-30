package com.archipio.templateservice.unittest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import com.archipio.templateservice.dto.RenderDto;
import com.archipio.templateservice.dto.TemplateConfigDto;
import com.archipio.templateservice.dto.TemplateOutputDto;
import com.archipio.templateservice.exception.InvalidTemplateArgumentsException;
import com.archipio.templateservice.exception.InvalidTemplateConfigurationFormatException;
import com.archipio.templateservice.exception.TemplateCodeAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateNameAlreadyExistsException;
import com.archipio.templateservice.exception.TemplateNotFoundException;
import com.archipio.templateservice.mapper.TemplateMapper;
import com.archipio.templateservice.persistence.entity.Parameter;
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
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
  @Mock private TemplateEngine customTemplateEngine;

  @InjectMocks private TemplateServiceImpl templateService;

  private static Stream<Arguments> provideParams() {
    return Stream.of(
        Arguments.of(
            List.of(
                RenderDto.ParameterDto.builder().name("param1").value("value1").build(),
                RenderDto.ParameterDto.builder().name("param2").value("value2").build(),
                RenderDto.ParameterDto.builder().name("param2").value("value3").build()),
            Set.of(
                Parameter.builder().name("param1").required(true).build(),
                Parameter.builder().name("param2").required(true).build(),
                Parameter.builder().name("param3").required(false).build())),
        Arguments.of(
            List.of(RenderDto.ParameterDto.builder().name("param1").value("value1").build()),
            Set.of(
                Parameter.builder().name("param1").required(true).build(),
                Parameter.builder().name("param2").required(true).build(),
                Parameter.builder().name("param3").required(false).build())),
        Arguments.of(
            List.of(
                RenderDto.ParameterDto.builder().name("param1").value("value1").build(),
                RenderDto.ParameterDto.builder().name("param2").value("value2").build(),
                RenderDto.ParameterDto.builder().name("param3").value("value3").build(),
                RenderDto.ParameterDto.builder().name("param4").value("value4").build()),
            Set.of(
                Parameter.builder().name("param1").required(true).build(),
                Parameter.builder().name("param2").required(true).build(),
                Parameter.builder().name("param3").required(false).build())));
  }

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
    assertThatExceptionOfType(TemplateNotFoundException.class)
        .isThrownBy(() -> templateService.deleteTemplate(code));

    // Check
    verify(templateRepository, times(1)).findByCode(code);
  }

  @Test
  @Order(2)
  public void
      renderTemplate_whenTemplateExistsAndParametersIsValidAndRenderIsSuccessful_thenReturnHtml()
          throws NoSuchFieldException, IllegalAccessException, IOException {
    // Prepare
    final var code = "code";
    final var params =
        List.of(
            RenderDto.ParameterDto.builder().name("param1").value("value1").build(),
            RenderDto.ParameterDto.builder().name("param2").value("value2").build());
    final var configParams =
        Set.of(
            Parameter.builder().name("param1").required(true).build(),
            Parameter.builder().name("param2").required(true).build(),
            Parameter.builder().name("param3").required(false).build());
    final var renderDto = RenderDto.builder().code(code).parameters(params).build();
    final var template = new Template();
    template.setCode(code);
    template.setParameters(configParams);

    // Create template file
    var storagePath = Path.of(getStoragePathValue()).toAbsolutePath().toString();
    var templateFilePath = storagePath + "/" + code + ".html";
    Files.createDirectories(Path.of(storagePath));
    FileUtils.copyFile(new File(RESOURCES_DIR_PATH + "/test.html"), new File(templateFilePath));
    var html = "<p>Success</p>";

    when(templateRepository.findByCode(code)).thenReturn(Optional.of(template));
    when(customTemplateEngine.process(eq(templateFilePath), any(Context.class))).thenReturn(html);

    // Do
    var actualHtml = templateService.renderTemplate(renderDto);

    // Check
    verify(templateRepository, times(1)).findByCode(code);
    verify(customTemplateEngine, times(1)).process(eq(templateFilePath), any(Context.class));
    assertThat(actualHtml).isEqualTo(html);

    // Return state
    FileUtils.delete(new File(templateFilePath));
  }

  @Test
  public void renderTemplate_whenTemplateConfigNotExists_thenThrownTemplateNotFoundException() {
    // Prepare
    final var code = "code";
    final var renderDto = RenderDto.builder().code(code).build();

    when(templateRepository.findByCode(code)).thenReturn(Optional.empty());

    // Do
    assertThatExceptionOfType(TemplateNotFoundException.class)
        .isThrownBy(() -> templateService.renderTemplate(renderDto));

    // Check
    verify(templateRepository, times(1)).findByCode(code);
  }

  @Test
  public void
  renderTemplate_whenTemplateConfigExistsAndTemplateFileNotFound_thenThrownTemplateNotFoundException() {
    // Prepare
    final var code = "code";
    final var renderDto = RenderDto.builder().code(code).build();
    final var template = new Template();
    template.setCode(code);

    when(templateRepository.findByCode(code)).thenReturn(Optional.of(template));

    // Do
    assertThatExceptionOfType(TemplateNotFoundException.class)
            .isThrownBy(() -> templateService.renderTemplate(renderDto));

    // Check
    verify(templateRepository, times(1)).findByCode(code);
  }

  @ParameterizedTest
  @MethodSource("provideParams")
  public void
      renderTemplate_whenTemplateExistsAndParametersIsInvalid_thenThrownInvalidArgumentsException(
          List<RenderDto.ParameterDto> params, Set<Parameter> configParams)
          throws NoSuchFieldException, IllegalAccessException, IOException {
    // Prepare
    var code = "code";
    var renderDto = RenderDto.builder().code(code).parameters(params).build();
    var template = new Template();
    template.setCode(code);
    template.setParameters(configParams);

    // Create template file
    var storagePath = Path.of(getStoragePathValue()).toAbsolutePath().toString();
    var templateFilePath = storagePath + "/" + code + ".html";
    Files.createDirectories(Path.of(storagePath));
    FileUtils.copyFile(new File(RESOURCES_DIR_PATH + "/test.html"), new File(templateFilePath));

    when(templateRepository.findByCode(code)).thenReturn(Optional.of(template));

    // Do
    assertThatExceptionOfType(InvalidTemplateArgumentsException.class)
        .isThrownBy(() -> templateService.renderTemplate(renderDto));

    // Return state
    FileUtils.delete(new File(templateFilePath));
  }

  @ParameterizedTest
  @CsvSource(
      value = {"1,2,1,2", "-1,2,0,2", "1,0,1,1", "null,2,0,2", "1,null,1,1"},
      nullValues = "null")
  public void getTemplates(
      Integer pageNumber, Integer pageSize, Integer actualPageNumber, Integer actualPageSize) {
    // Prepare
    final var template = new Template();
    final var templatePage =
        new PageImpl<>(
            List.of(template),
            PageRequest.of(actualPageNumber, actualPageSize, Sort.by("name")),
            1);
    final var templateOutputDto = new TemplateOutputDto();
    final var templateOutputDtoList = List.of(templateOutputDto);

    when(templateRepository.findAll(
            pageableThat(PageRequest.of(actualPageNumber, actualPageSize, Sort.by("name")))))
        .thenReturn(templatePage);
    when(templateMapper.toOutputDto(template)).thenReturn(templateOutputDto);

    // Do
    var actual = templateService.getTemplates(pageNumber, pageSize);

    // Check
    verify(templateRepository, times(1))
        .findAll(pageableThat(PageRequest.of(actualPageNumber, actualPageSize, Sort.by("name"))));
    verify(templateMapper, times(1)).toOutputDto(template);

    assertThat(actual).containsExactlyInAnyOrderElementsOf(templateOutputDtoList);
  }

  @Test
  public void getTemplate_whenTemplateExists_thenReturnTemplateOutputDto() {
    // Prepare
    final var code = "code";
    final var template = new Template();
    final var templateOutputDto = new TemplateOutputDto();

    when(templateRepository.findByCode(code)).thenReturn(Optional.of(template));
    when(templateMapper.toOutputDto(template)).thenReturn(templateOutputDto);

    // Do
    var actualTemplateOutputDto = templateService.getTemplate(code);

    // Check
    verify(templateRepository, times(1)).findByCode(code);
    verify(templateMapper, times(1)).toOutputDto(template);

    assertThat(actualTemplateOutputDto).isEqualTo(templateOutputDto);
  }

  @Test
  public void getTemplate_whenTemplateNotExists_thenThrownTemplateNotFoundException() {
    // Prepare
    final var code = "code";

    when(templateRepository.findByCode(code)).thenReturn(Optional.empty());

    // Do
    assertThatExceptionOfType(TemplateNotFoundException.class)
        .isThrownBy(() -> templateService.getTemplate(code));

    // Check
    verify(templateRepository, times(1)).findByCode(code);
  }

  private String getStoragePathValue() throws NoSuchFieldException, IllegalAccessException {
    Field field = TemplateServiceImpl.class.getDeclaredField("STORAGE_PATH");
    field.setAccessible(true);
    return (String) field.get(null);
  }

  private Pageable pageableThat(Pageable value) {
    return argThat(new PageableThat(value));
  }

  @RequiredArgsConstructor
  private static class PageableThat implements ArgumentMatcher<Pageable> {

    private final Pageable pageable;

    @Override
    public boolean matches(Pageable argument) {
      return argument.getPageNumber() == pageable.getPageNumber()
          && argument.getPageSize() == pageable.getPageSize()
          && argument.getSort().equals(pageable.getSort());
    }

    @Override
    public Class<?> type() {
      return Pageable.class;
    }
  }
}
