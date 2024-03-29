package com.archipio.templateservice.persistence.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parameter implements Serializable {

  private String name;

  @EqualsAndHashCode.Exclude private Boolean required;
}
