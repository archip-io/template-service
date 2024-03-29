package com.archipio.templateservice.controller.api.v0;

import static com.archipio.templateservice.util.ApiUtils.API_V0_PREFIX;
import static com.archipio.templateservice.util.ApiUtils.IMPORT_SUFFIX;
import static org.springframework.http.HttpStatus.OK;

import com.archipio.templateservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_V0_PREFIX)
public class TemplateController {

  private final TemplateService templateService;

  @PostMapping(value = IMPORT_SUFFIX, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> importTemplate(@RequestPart("data") MultipartFile data) {
    templateService.importTemplate(data);
    return ResponseEntity.status(OK).build();
  }
}
