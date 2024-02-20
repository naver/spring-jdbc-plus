package com.navercorp.spring.data.jdbc.plus.sql.guide.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig {
	private final JdbcMappingContext mappingContext;

	@PostConstruct
	public void init() {
		this.mappingContext.setSingleQueryLoadingEnabled(true);
	}
}
