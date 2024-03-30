package com.archipio.templateservice.unittest.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.archipio.templateservice.dto.RenderDto;
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

public class RenderDtoTest {

  private Validator validator;

  private static Stream<Arguments> provideInvalidRenderDto() {
    return Stream.of(
        Arguments.of(
            RenderDto.builder()
                .code(null)
                .parameters(
                    List.of(RenderDto.ParameterDto.builder().name("param").value("value").build()))
                .build(),
            Set.of("code")),
        Arguments.of(
            RenderDto.builder()
                .code("template")
                .parameters(
                    List.of(RenderDto.ParameterDto.builder().name(null).value("value").build()))
                .build(),
            Set.of("parameters[0].name")),
        Arguments.of(
            RenderDto.builder()
                .code("template")
                .parameters(
                    List.of(RenderDto.ParameterDto.builder().name("param").value(null).build()))
                .build(),
            Set.of("parameters[0].value")));
  }

  @BeforeEach
  public void setUp() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @ParameterizedTest
  @MethodSource("provideInvalidRenderDto")
  public void validate_whenRenderDtoIsInvalid_thenViolationsIsNotEmpty(
      RenderDto RenderDto, Set<String> expectedErrorFields) {
    // Do
    var violations = validator.validate(RenderDto);
    var actualErrorFields =
        violations.stream()
            .map(constraintViolation -> constraintViolation.getPropertyPath().toString())
            .collect(Collectors.toSet());

    // Check
    assertThat(violations.isEmpty()).isFalse();
    assertThat(actualErrorFields).containsExactlyInAnyOrderElementsOf(expectedErrorFields);
  }
}
