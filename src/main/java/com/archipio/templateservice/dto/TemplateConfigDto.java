package com.archipio.templateservice.dto;

import static com.archipio.templateservice.util.ValidationUtils.TEMPLATE_CODE_MAX_LENGTH;
import static com.archipio.templateservice.util.ValidationUtils.TEMPLATE_CODE_MIN_LENGTH;
import static com.archipio.templateservice.util.ValidationUtils.TEMPLATE_CODE_PATTERN;
import static com.archipio.templateservice.util.ValidationUtils.TEMPLATE_NAME_MAX_LENGTH;
import static com.archipio.templateservice.util.ValidationUtils.TEMPLATE_NAME_MIN_LENGTH;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateConfigDto {

  @NotNull(message = "{validation.template.name.not-null}")
  @Length(
      message = "{validation.template.name.length}",
      min = TEMPLATE_NAME_MIN_LENGTH,
      max = TEMPLATE_NAME_MAX_LENGTH)
  private String name;

  @NotNull(message = "{validation.template.code.not-null}")
  @Length(
      message = "{validation.template.code.length}",
      min = TEMPLATE_CODE_MIN_LENGTH,
      max = TEMPLATE_CODE_MAX_LENGTH)
  @Pattern(regexp = TEMPLATE_CODE_PATTERN, message = "{validation.template.code.pattern}")
  private String code;

  private String description;

  @Valid private List<ParameterDto> parameters = new ArrayList<>();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ParameterDto {

    @NotNull(message = "{validation.parameter.name.not-null}")
    private String name;

    private Boolean required = true;
  }
}
