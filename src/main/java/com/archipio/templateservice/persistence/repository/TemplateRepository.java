package com.archipio.templateservice.persistence.repository;

import com.archipio.templateservice.persistence.entity.Template;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Integer> {

  boolean existsByName(String name);

  boolean existsByCode(String code);

  Optional<Template> findByCode(String code);
}
