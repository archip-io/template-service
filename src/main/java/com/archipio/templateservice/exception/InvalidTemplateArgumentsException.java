package com.archipio.templateservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidTemplateArgumentsException extends RuntimeException {

  private final String message;
}
