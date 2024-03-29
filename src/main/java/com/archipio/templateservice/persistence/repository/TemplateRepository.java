package com.archipio.templateservice.persistence.repository;

import com.archipio.templateservice.persistence.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Integer> {

  boolean existsByName(String name);

  boolean existsByCode(String code);

  Optional<Template> findByCode(String code);
}
