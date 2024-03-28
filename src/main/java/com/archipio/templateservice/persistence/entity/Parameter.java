package com.archipio.templateservice.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parameter implements Serializable {

    private String name;

    @EqualsAndHashCode.Exclude
    private boolean required;
}
