package com.archipio.templateservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenderDto {

  @NotNull(message = "{validation.template.code.not-null}")
  private String code;

  @Valid private List<ParameterDto> parameters = new ArrayList<>();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ParameterDto {

    @NotNull(message = "{validation.parameter.name.not-null}")
    private String name;

    @NotNull(message = "{validation.parameter.value.not-null}")
    private String value;
  }
}
