package com.archipio.templateservice.unittest.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.archipio.templateservice.dto.ErrorDto;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
public class ErrorDtoTest {

  @Autowired private JacksonTester<ErrorDto> errorDtoJson;

  @Test
  public void checkSerializationOfErrorDtoWithoutErrors() throws IOException {
    // Prepare
    final Instant createdAt = Instant.now();
    final String message = "Message";
    ErrorDto errorDto =
        ErrorDto.builder().createdAt(createdAt).message(message).errors(null).build();

    // Do
    JsonContent<ErrorDto> result = errorDtoJson.write(errorDto);

    // Check
    assertThat(result).hasJsonPathStringValue("$.created_at");
    assertThat(result)
        .extractingJsonPathStringValue("$.created_at")
        .isEqualTo(createdAt.toString());
    assertThat(result).hasJsonPathStringValue("$.message");
    assertThat(result).extractingJsonPathStringValue("$.message").isEqualTo(message);
    assertThat(result).doesNotHaveJsonPath("$.errors");
  }

  @Test
  public void checkSerializationOfErrorDtoWithErrors() throws IOException {
    // Prepare
    final Instant createdAt = Instant.now();
    final String message = "Message";
    final Map<String, String> errors = Map.of("field1", "error1", "field2", "error2");
    ErrorDto errorDto =
        ErrorDto.builder().createdAt(createdAt).message(message).errors(errors).build();

    // Do
    JsonContent<ErrorDto> result = errorDtoJson.write(errorDto);

    // Check
    assertThat(result).hasJsonPathStringValue("$.created_at");
    assertThat(result)
        .extractingJsonPathStringValue("$.created_at")
        .isEqualTo(createdAt.toString());
    assertThat(result).hasJsonPathStringValue("$.message");
    assertThat(result).extractingJsonPathStringValue("$.message").isEqualTo(message);
    assertThat(result).hasJsonPathMapValue("$.errors");
    assertThat(result).extractingJsonPathMapValue("$.errors").isEqualTo(errors);
  }
}
