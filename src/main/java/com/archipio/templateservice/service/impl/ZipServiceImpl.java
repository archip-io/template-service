package com.archipio.templateservice.service.impl;

import com.archipio.templateservice.dto.TemplateZipDto;
import com.archipio.templateservice.exception.InvalidZipFormatException;
import com.archipio.templateservice.service.ZipService;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class ZipServiceImpl implements ZipService {

  @Override
  public TemplateZipDto extractAll(File zip) throws IOException {
    var templateZipDto = new TemplateZipDto();

    try (var zipFile = new ZipFile(zip)) {
      var entries = zipFile.entries();
      var fileCount = 0;

      while (entries.hasMoreElements()) {
        var entry = entries.nextElement();

        if (entry.isDirectory()) {
          throw new InvalidZipFormatException("exception.invalid-zip-format.there-is-directory");
        }

        fileCount++;
        String fileName = entry.getName();
        if (fileName.endsWith(".html")) {
          var htmlFile = File.createTempFile(fileName, "");
          FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), htmlFile);
          templateZipDto.setHtmlTemplate(htmlFile);
        } else if (fileName.endsWith(".json")) {
          var jsonFile = File.createTempFile(fileName, "");
          FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), jsonFile);
          templateZipDto.setJsonConfig(jsonFile);
        } else {
          throw new InvalidZipFormatException(
              "exception.invalid-zip-format.invalid-file-extension");
        }
      }

      if (fileCount < 2) {
        throw new InvalidZipFormatException("exception.invalid-zip-format.too-few-files");
      }
      if (fileCount > 2) {
        throw new InvalidZipFormatException("exception.invalid-zip-format.too-many-files");
      }
    } catch (ZipException e) {
      throw new InvalidZipFormatException("exception.invalid-zip-format.not-zip-archive");
    }

    return templateZipDto;
  }
}
