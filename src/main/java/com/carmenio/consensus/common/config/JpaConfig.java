package com.carmenio.consensus.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration.
 * <p>
 * Enables JPA auditing for automatic timestamp management
 * and transaction management.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
