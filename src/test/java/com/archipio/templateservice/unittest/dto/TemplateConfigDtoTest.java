package com.archipio.templateservice.unittest.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.archipio.templateservice.dto.TemplateConfigDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TemplateConfigDtoTest {

  private Validator validator;

  private static Stream<Arguments> provideInvalidTemplateConfigDto() {
    return Stream.of(
        Arguments.of(
            TemplateConfigDto.builder()
                .name(null)
                .code("template")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("name")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name("")
                .code("template")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("name")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name(
                    "1111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111111111111111111111111111111111111111")
                .code("template")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("name")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name("Template")
                .code(null)
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("code")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name("Template")
                .code("template!")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("code")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name("Template")
                .code("")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("code")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name("Template")
                .code(
                    "1111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111111111111111111111111111111111111111")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name("param").build()))
                .build(),
            Set.of("code")),
        Arguments.of(
            TemplateConfigDto.builder()
                .name("Template")
                .code("template")
                .parameters(List.of(TemplateConfigDto.ParameterDto.builder().name(null).build()))
                .build(),
            Set.of("parameters[0].name")));
  }

  @BeforeEach
  public void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @ParameterizedTest
  @MethodSource("provideInvalidTemplateConfigDto")
  public void validate_whenTemplateConfigDtoIsInvalid_thenViolationsIsNotEmpty(
      TemplateConfigDto templateConfigDto, Set<String> expectedErrorFields) {
    // Do
    var violations = validator.validate(templateConfigDto);
    var actualErrorFields =
        violations.stream()
            .map(constraintViolation -> constraintViolation.getPropertyPath().toString())
            .collect(Collectors.toSet());

    // Check
    assertThat(violations.isEmpty()).isFalse();
    assertThat(actualErrorFields).containsExactlyInAnyOrderElementsOf(expectedErrorFields);
  }
}
