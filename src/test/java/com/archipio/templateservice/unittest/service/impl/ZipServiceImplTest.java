package com.archipio.templateservice.unittest.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.quality.Strictness.LENIENT;

import com.archipio.templateservice.exception.InvalidZipFormatException;
import com.archipio.templateservice.service.impl.ZipServiceImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ZipServiceImplTest {

  private static final String RESOURCES_DIR_PATH =
      Paths.get("src", "test", "resources").toFile().getAbsolutePath();

  @InjectMocks ZipServiceImpl zipService;

  @Test
  public void extractAll_whenZipIsValid_thenReturnTemplateZipDto() throws IOException {
    // Prepare
    var zipFile = new File(RESOURCES_DIR_PATH + "/valid.zip");

    // Do
    var templateZipDto = zipService.extractAll(zipFile);

    // Check
    assertThat(templateZipDto.getHtmlTemplate()).isNotNull();
    assertThat(templateZipDto.getJsonConfig()).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "not-zip.txt",
        "there_is_directory.zip",
        "invalid_file_extension.zip",
        "too_few_files.zip",
        "too_many_files.zip"
      })
  public void extractAll_whenNotZip_thenThrownInvalidZipFormatException(String zipFileName) {
    // Prepare
    var zipFile = new File(RESOURCES_DIR_PATH + "/" + zipFileName);

    // Do and Check
    assertThatExceptionOfType(InvalidZipFormatException.class)
        .isThrownBy(() -> zipService.extractAll(zipFile));
  }
}
