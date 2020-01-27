package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import java.util.Map;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class DefaultSqlParameterSourceFactory implements SqlParameterSourceFactory {
	private final RelationalMappingContext mappingContext;
	private final IdentifierProcessing identifierProcessing;
	private final EntitySqlParameterSourceApplier parameterSourceApplier;

	public DefaultSqlParameterSourceFactory(RelationalMappingContext mappingContext, JdbcConverter jdbcConverter, IdentifierProcessing identifierProcessing) {
		this.mappingContext = mappingContext;
		this.identifierProcessing = identifierProcessing;
		this.parameterSourceApplier = new EntitySqlParameterSourceApplier(mappingContext, jdbcConverter);
	}

	@Override
	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return new BeanPropertySqlParameterSource(bean);
	}

	@Override
	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return new MapSqlParameterSource(map);
	}

	@Override
	public SqlParameterSource entityParameterSource(Object entity) {
		SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource(this.identifierProcessing);
		RelationalPersistentEntity<?> persistentEntity =
			this.mappingContext.getRequiredPersistentEntity(entity.getClass());
		this.parameterSourceApplier.addParameterSource(parameterSource, entity, persistentEntity, "");
		return parameterSource;
	}
}
