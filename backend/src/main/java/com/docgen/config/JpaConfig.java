package com.docgen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables JPA auditing (so {@code @CreatedDate}/{@code @LastModifiedDate} on
 * {@link com.docgen.common.BaseEntity} are populated) and repository scanning.
 *
 * <p>{@code considerNestedRepositories = true} lets us group small repository interfaces inside
 * per-aggregate holder classes (e.g. {@code TemplateRepositories.FormTemplateRepository}).
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.docgen", considerNestedRepositories = true)
public class JpaConfig {
}
