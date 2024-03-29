package com.archipio.templateservice.service;

import com.archipio.templateservice.dto.TemplateZipDto;
import java.io.File;
import java.io.IOException;

public interface ZipService {

  TemplateZipDto extractAll(File zipFile) throws IOException;
}
