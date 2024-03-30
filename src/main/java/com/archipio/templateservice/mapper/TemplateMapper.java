package com.archipio.templateservice.mapper;

import com.archipio.templateservice.dto.TemplateConfigDto;
import com.archipio.templateservice.dto.TemplateOutputDto;
import com.archipio.templateservice.persistence.entity.Template;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface TemplateMapper {

  Template toEntity(TemplateConfigDto templateConfigDto);

  TemplateOutputDto toOutputDto(Template template);
}
