package com.navercorp.spring.data.jdbc.plus.sql.guide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayHistory;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig {
	private final JdbcMappingContext mappingContext;

	@PostConstruct
	public void init() {
		this.mappingContext.setSingleQueryLoadingEnabled(true);
	}

	@Bean
	public BeforeConvertCallback<PayHistory> generatePayHistoryNo() {
		return (aggregate) -> {
			if (!aggregate.isNew()) {
				return aggregate;
			} else {
				return aggregate.generateId();
			}
		};
	}
}
