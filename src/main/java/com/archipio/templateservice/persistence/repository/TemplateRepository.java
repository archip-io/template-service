package com.archipio.templateservice.persistence.repository;

import com.archipio.templateservice.persistence.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Integer> {}
