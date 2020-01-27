package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import java.util.Map;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.navercorp.spring.jdbc.plus.support.parametersource.ConvertibleParameterSourceFactory;

public class EntityConvertibleSqlParameterSourceFactory implements SqlParameterSourceFactory {
	private final ConvertibleParameterSourceFactory delegate;
	private final RelationalMappingContext mappingContext;
	private final IdentifierProcessing identifierProcessing;
	private final EntitySqlParameterSourceApplier parameterSourceApplier;

	public EntityConvertibleSqlParameterSourceFactory(
		ConvertibleParameterSourceFactory delegate,
		RelationalMappingContext mappingContext,
		JdbcConverter jdbcConverter,
		IdentifierProcessing identifierProcessing) {

		this.delegate = delegate;
		this.mappingContext = mappingContext;
		this.identifierProcessing = identifierProcessing;
		this.parameterSourceApplier = new EntitySqlParameterSourceApplier(mappingContext, jdbcConverter);
	}

	@Override
	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return this.delegate.beanParameterSource(bean);
	}

	@Override
	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return this.delegate.mapParameterSource(map);
	}

	@Override
	public SqlParameterSource entityParameterSource(Object entity) {
		SqlIdentifierParameterSource parameterSource = new ConvertibleSqlIdentifierParameterSource(
			this.identifierProcessing, this.delegate.getConverter(), this.delegate.getFallback());
		RelationalPersistentEntity<?> persistentEntity =
			this.mappingContext.getRequiredPersistentEntity(entity.getClass());
		this.parameterSourceApplier.addParameterSource(parameterSource, entity, persistentEntity, "");
		return parameterSource;
	}
}
