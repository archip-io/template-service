package com.archipio.templateservice.controller.sys.v0;

import static com.archipio.templateservice.util.ApiUtils.RENDER_SUFFIX;
import static com.archipio.templateservice.util.ApiUtils.SYS_V0_PREFIX;
import static org.springframework.http.HttpStatus.OK;

import com.archipio.templateservice.dto.RenderDto;
import com.archipio.templateservice.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("sysTemplateController")
@RequiredArgsConstructor
@RequestMapping(SYS_V0_PREFIX)
public class TemplateController {

  private final TemplateService templateService;

  @PostMapping(value = RENDER_SUFFIX, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> renderTemplate(@Valid @RequestBody RenderDto renderDto) {
    var html = templateService.renderTemplate(renderDto);
    return ResponseEntity.status(OK).contentType(MediaType.TEXT_HTML).body(html);
  }
}
