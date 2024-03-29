package com.archipio.templateservice.dto;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateZipDto {

  private File htmlTemplate;
  private File jsonConfig;
}
