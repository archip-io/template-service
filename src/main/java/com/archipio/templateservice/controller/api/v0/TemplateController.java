package com.archipio.templateservice.controller.api.v0;

import static com.archipio.templateservice.util.ApiUtils.API_V0_PREFIX;
import static com.archipio.templateservice.util.ApiUtils.DELETE_SUFFIX;
import static com.archipio.templateservice.util.ApiUtils.GET_SUFFIX;
import static com.archipio.templateservice.util.ApiUtils.IMPORT_SUFFIX;
import static com.archipio.templateservice.util.ApiUtils.RENDER_SUFFIX;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.archipio.templateservice.dto.RenderDto;
import com.archipio.templateservice.dto.TemplateOutputDto;
import com.archipio.templateservice.service.TemplateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("apiTemplateController")
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

  @PreAuthorize("hasAuthority('RENDER_TEMPLATE')")
  @PostMapping(value = RENDER_SUFFIX, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> renderTemplate(@Valid @RequestBody RenderDto renderDto) {
    var html = templateService.renderTemplate(renderDto);
    return ResponseEntity.status(OK).contentType(MediaType.TEXT_HTML).body(html);
  }

  @PreAuthorize("hasAuthority('VIEW_TEMPLATE')")
  @GetMapping(GET_SUFFIX)
  public ResponseEntity<List<TemplateOutputDto>> getTemplates(
      @RequestParam(value = "page", required = false) Integer pageNumber,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    return ResponseEntity.status(OK).body(templateService.getTemplates(pageNumber, pageSize));
  }

  @PreAuthorize("hasAuthority('VIEW_TEMPLATE')")
  @GetMapping(GET_SUFFIX + "/{code}")
  public ResponseEntity<TemplateOutputDto> getTemplate(@PathVariable("code") String code) {
    return ResponseEntity.status(OK).body(templateService.getTemplate(code));
  }

  @PreAuthorize("hasAuthority('DELETE_TEMPLATE')")
  @DeleteMapping(DELETE_SUFFIX + "/{code}")
  public ResponseEntity<Void> deleteTemplate(@PathVariable("code") String code) {
    templateService.deleteTemplate(code);
    return ResponseEntity.status(NO_CONTENT).build();
  }
}
