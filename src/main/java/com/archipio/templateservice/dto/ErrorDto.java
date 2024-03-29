package com.archipio.templateservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDto {

  @JsonProperty("created_at")
  private Instant createdAt;

  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, String> errors;
}
