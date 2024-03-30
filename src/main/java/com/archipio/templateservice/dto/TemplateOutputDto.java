package com.archipio.templateservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateOutputDto {

  private String name;
  private String code;
  private String description;
  private List<ParameterDto> parameters;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ParameterDto {

    private String name;
    private Boolean required;
  }
}
