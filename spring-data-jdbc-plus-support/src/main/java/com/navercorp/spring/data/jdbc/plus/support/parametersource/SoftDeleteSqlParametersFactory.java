package com.navercorp.spring.data.jdbc.plus.support.parametersource;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.TypeInformation;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.support.convert.SoftDeleteProperty;

public class SoftDeleteSqlParametersFactory {

	private final RelationalMappingContext context;
	private final JdbcConverter converter;

	public SoftDeleteSqlParametersFactory(
		RelationalMappingContext context,
		JdbcConverter converter
	) {
		this.context = context;
		this.converter = converter;
	}

	public <T> SqlParameterSource forSoftDeleteById(
		Object id,
		Class<T> domainType,
		SqlIdentifier name,
		SoftDeleteProperty softDeleteProperty
	) {
		SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();

		addConvertedPropertyValue(
			parameterSource,
			getRequiredPersistentEntity(domainType).getRequiredIdProperty(),
			id,
			name
		);

		addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

		return parameterSource;
	}

	public <T> SqlParameterSource forSoftDeleteByIdWithVersion(
		Object id,
		Class<T> domainType,
		SqlIdentifier name,
		SoftDeleteProperty softDeleteProperty,
		SqlIdentifier versionPropertyName,
		Number previousVersion
	) {
		SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();

		addConvertedPropertyValue(
			parameterSource,
			getRequiredPersistentEntity(domainType).getRequiredIdProperty(),
			id,
			name
		);

		parameterSource.addValue(versionPropertyName, previousVersion);
		addVersionToUpdatePropertyValue(domainType, previousVersion, parameterSource);

		addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

		return parameterSource;
	}

	public <T> SqlParameterSource forSoftDeleteAll(SoftDeleteProperty softDeleteProperty) {
		SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();

		addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

		return parameterSource;
	}

	public <T> SqlParameterSource forSoftDeleteByIds(
		Iterable<?> ids,
		Class<T> domainType,
		SqlIdentifier idsPropertyName,
		SoftDeleteProperty softDeleteProperty
	) {

		SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();

		addConvertedPropertyValuesAsList(
			parameterSource,
			getRequiredPersistentEntity(domainType).getRequiredIdProperty(),
			ids,
			idsPropertyName
		);

		addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

		return parameterSource;
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.SqlParametersFactory#getRequiredPersistentEntity}
	 */
	@SuppressWarnings("unchecked")
	private <S> RelationalPersistentEntity<S> getRequiredPersistentEntity(Class<S> domainType) {
		return (RelationalPersistentEntity<S>)context.getRequiredPersistentEntity(domainType);
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.SqlParametersFactory#addConvertedPropertyValue}
	 */
	private void addConvertedPropertyValue(
		SqlIdentifierParameterSource parameterSource,
		RelationalPersistentProperty property,
		@Nullable Object value,
		SqlIdentifier name
	) {

		addConvertedValue(
			parameterSource,
			value,
			name,
			converter.getColumnType(property),
			converter.getTargetSqlType(property)
		);
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.SqlParametersFactory#addConvertedPropertyValuesAsList}
	 *
	 * diff: additional 'idsPropertyName' parameter because of SqlGenerator's access modifier
	 */
	private void addConvertedPropertyValuesAsList(
		SqlIdentifierParameterSource parameterSource,
		RelationalPersistentProperty property,
		Iterable<?> values,
		SqlIdentifier idsPropertyName
	) {

		List<Object> convertedIds = new ArrayList<>();
		JdbcValue jdbcValue = null;
		for (Object id : values) {

			Class<?> columnType = converter.getColumnType(property);
			SQLType sqlType = converter.getTargetSqlType(property);

			jdbcValue = converter.writeJdbcValue(id, columnType, sqlType);
			convertedIds.add(jdbcValue.getValue());
		}

		Assert.state(jdbcValue != null, "JdbcValue must be not null at this point; Please report this as a bug");

		SQLType jdbcType = jdbcValue.getJdbcType();
		int typeNumber = jdbcType == null ? JdbcUtils.TYPE_UNKNOWN : jdbcType.getVendorTypeNumber();

		parameterSource.addValue(idsPropertyName, convertedIds, typeNumber);
	}

	private void addConvertedValue(
		SqlIdentifierParameterSource parameterSource,
		@Nullable Object value,
		SqlIdentifier paramName,
		Class<?> javaType,
		SQLType sqlType
	) {

		JdbcValue jdbcValue = converter.writeJdbcValue(
			value,
			javaType,
			sqlType
		);

		parameterSource.addValue(
			paramName,
			jdbcValue.getValue(),
			jdbcValue.getJdbcType().getVendorTypeNumber()
		);
	}

	private <S> void addVersionToUpdatePropertyValue(
		Class<S> domainType,
		Number previousVersion,
		SqlIdentifierParameterSource parameterSource
	) {
		RelationalPersistentEntity<S> persistentEntity = getRequiredPersistentEntity(domainType);

		persistentEntity.doWithAll(property -> {
			if (property.isVersionProperty()) {
				SqlIdentifier paramName = property.getColumnName();

				addConvertedPropertyValue(
					parameterSource,
					property,
					previousVersion.longValue() + 1,
					paramName
				);
			}
		});
	}

	private void addSoftDeletePropertyValue(
		SoftDeleteProperty softDeleteProperty,
		SqlIdentifierParameterSource parameterSource
	) {
		Object updateValue = softDeleteProperty.getUpdateValue();

		if (updateValue instanceof Boolean) {
			boolean booleanValue = Boolean.TRUE.equals(updateValue);

			parameterSource.addValue(
				softDeleteProperty.getColumnName(),
				booleanValue ? 1 : 0,
				JDBCType.INTEGER.getVendorTypeNumber()
			);
		} else {
			parameterSource.addValue(
				softDeleteProperty.getColumnName(),
				converter.writeValue(updateValue, TypeInformation.of(updateValue.getClass())),
				JDBCType.VARCHAR.getVendorTypeNumber()
			);
		}
	}
}
