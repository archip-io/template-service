package com.archipio.templateservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InvalidZipFormatException extends RuntimeException {

  private final String message;
}
