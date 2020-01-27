package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcValue;
import org.springframework.data.jdbc.support.JdbcUtil;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.lang.Nullable;

class EntitySqlParameterSourceApplier {
	private final RelationalMappingContext mappingContext;
	private final JdbcConverter jdbcConverter;

	EntitySqlParameterSourceApplier(RelationalMappingContext mappingContext, JdbcConverter jdbcConverter) {
		this.mappingContext = mappingContext;
		this.jdbcConverter = jdbcConverter;
	}

	// DefaultDataAccessStrategy#getParameterSource
	void addParameterSource(
		SqlIdentifierParameterSource parameterSource,
		Object instance,
		RelationalPersistentEntity<?> persistentEntity,
		String prefix) {

		PersistentPropertyAccessor<?> propertyAccessor = instance != null
			? persistentEntity.getPropertyAccessor(instance)
			: NoValuePropertyAccessor.instance();

		persistentEntity.doWithProperties((PropertyHandler<RelationalPersistentProperty>)property -> {
			if (!property.isWritable()) {
				return;
			}

			if (property.isEntity() && !property.isEmbedded()) {
				return;
			}

			if (property.isEmbedded()) {
				Object value = propertyAccessor.getProperty(property);
				RelationalPersistentEntity<?> embeddedEntity =
					this.mappingContext.getRequiredPersistentEntity(property.getType());
				this.addParameterSource(
					parameterSource, value, embeddedEntity, prefix + property.getEmbeddedPrefix());
			} else {
				Object value = propertyAccessor.getProperty(property);
				SqlIdentifier paramName = property.getColumnName().transform(prefix::concat);
				this.addConvertedPropertyValue(parameterSource, property, value, paramName);
			}
		});
	}

	// DefaultDataAccessStrategy#addConvertedPropertyValue
	private void addConvertedPropertyValue(
		SqlIdentifierParameterSource parameterSource, RelationalPersistentProperty property, Object value, SqlIdentifier name) {

		Class<?> javaType = this.jdbcConverter.getColumnType(property);
		int sqlType = this.jdbcConverter.getSqlType(property);
		JdbcValue jdbcValue = this.jdbcConverter.writeJdbcValue(value, javaType, sqlType);
		parameterSource.addValue(name, jdbcValue.getValue(), JdbcUtil.sqlTypeFor(jdbcValue.getJdbcType()));
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.DefaultDataAccessStrategy.NoValuePropertyAccessor}
	 */
	@SuppressWarnings("unchecked")
	private static class NoValuePropertyAccessor<T> implements PersistentPropertyAccessor<T> {

		private static final NoValuePropertyAccessor INSTANCE = new NoValuePropertyAccessor();

		static <T> NoValuePropertyAccessor<T> instance() {
			return INSTANCE;
		}

		@Override
		public void setProperty(PersistentProperty<?> property, @Nullable Object value) {
			throw new UnsupportedOperationException("Cannot set value on 'null' target object.");
		}

		@Override
		public Object getProperty(PersistentProperty<?> property) {
			return null;
		}

		@Override
		public T getBean() {
			return null;
		}
	}
}
