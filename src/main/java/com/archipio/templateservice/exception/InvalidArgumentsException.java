package com.archipio.templateservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidArgumentsException extends RuntimeException {

  private final String message;
}
