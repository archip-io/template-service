package com.archipio.templateservice.unittest.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.archipio.templateservice.persistence.entity.Template;
import com.archipio.templateservice.persistence.repository.TemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
class TemplateRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private TemplateRepository templateRepository;

  @Test
  void existsByName_whenNameExists_thenReturnTrue() {
    // Prepare
    final var name = "Template";
    final var template = new Template();
    template.setName(name);
    entityManager.persist(template);

    // Do
    var actual = templateRepository.existsByName(name);

    // Check
    assertThat(actual).isTrue();
  }

  @Test
  void existsByName_whenNameNotExists_thenReturnFalse() {
    // Prepare
    final var name = "Template";

    // Do
    var actual = templateRepository.existsByName(name);

    // Check
    assertThat(actual).isFalse();
  }

  @Test
  void existsByCode_whenCodeExists_thenReturnTrue() {
    // Prepare
    final var code = "template";
    final var template = new Template();
    template.setCode(code);
    entityManager.persist(template);

    // Do
    var actual = templateRepository.existsByCode(code);

    // Check
    assertThat(actual).isTrue();
  }

  @Test
  void existsByCode_whenCodeNotExists_thenReturnFalse() {
    // Prepare
    final var code = "template";

    // Do
    var actual = templateRepository.existsByCode(code);

    // Check
    assertThat(actual).isFalse();
  }
}
