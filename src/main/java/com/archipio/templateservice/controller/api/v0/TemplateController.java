package com.archipio.templateservice.controller.api.v0;

import static com.archipio.templateservice.util.ApiUtils.API_V0_PREFIX;
import static com.archipio.templateservice.util.ApiUtils.DELETE_SUFFIX;
import static com.archipio.templateservice.util.ApiUtils.IMPORT_SUFFIX;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.archipio.templateservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @PreAuthorize("hasAuthority('IMPORT_TEMPLATE')")
  @PostMapping(value = IMPORT_SUFFIX, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> importTemplate(@RequestPart("data") MultipartFile data) {
    templateService.importTemplate(data);
    return ResponseEntity.status(OK).build();
  }

  @PreAuthorize("hasAuthority('DELETE_TEMPLATE')")
  @DeleteMapping(DELETE_SUFFIX + "/{code}")
  public ResponseEntity<Void> deleteTemplate(@PathVariable("code") String code) {
    templateService.deleteTemplate(code);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
