package com.navercorp.spring.data.jdbc.plus.support.parametersource;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.TypeInformation;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;

import com.navercorp.spring.data.jdbc.plus.support.convert.SoftDeleteProperty;
import com.navercorp.spring.data.jdbc.plus.support.convert.SqlGenerator;

/**
 * Creates the {@link SqlParameterSource} for soft delete SQL operations.
 *
 * @see org.springframework.data.jdbc.core.convert.SqlParametersFactory
 */
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
		SoftDeleteProperty softDeleteProperty
	) {
		return doWithIdentifiers(domainType, (columns, idProperty, complexId) -> {

			SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();
			BiFunction<Object, AggregatePath, Object> valueExtractor = getIdMapper(complexId);

			columns.forEach((ap, ci) -> addConvertedPropertyValue( //
				parameterSource, //
				ap.getRequiredLeafProperty(), //
				valueExtractor.apply(id, ap), //
				ci.name() //
			));

			addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

			return parameterSource;
		});
	}

	public <T> SqlParameterSource forSoftDeleteByIdWithVersion(
		Object id,
		Class<T> domainType,
		SoftDeleteProperty softDeleteProperty,
		SqlIdentifier versionPropertyName,
		Number previousVersion
	) {
		return doWithIdentifiers(domainType, (columns, idProperty, complexId) -> {

			SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();
			BiFunction<Object, AggregatePath, Object> valueExtractor = getIdMapper(complexId);

			columns.forEach((ap, ci) -> addConvertedPropertyValue( //
				parameterSource, //
				ap.getRequiredLeafProperty(), //
				valueExtractor.apply(id, ap), //
				ci.name() //
			));

			parameterSource.addValue(versionPropertyName, previousVersion);
			addVersionToUpdatePropertyValue(domainType, previousVersion, parameterSource);

			addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

			return parameterSource;
		});
	}

	public <T> SqlParameterSource forSoftDeleteAll(SoftDeleteProperty softDeleteProperty) {
		SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();

		addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

		return parameterSource;
	}

	public <T> SqlParameterSource forSoftDeleteByIds(
		Iterable<?> ids,
		Class<T> domainType,
		SoftDeleteProperty softDeleteProperty
	) {

		return doWithIdentifiers(domainType, (columns, idProperty, complexId) -> {

			SqlIdentifierParameterSource parameterSource = new SqlIdentifierParameterSource();

			BiFunction<Object, AggregatePath, Object> valueExtractor = getIdMapper(complexId);

			List<Object[]> parameterValues = new ArrayList<>(ids instanceof Collection<?> c ? c.size() : 16);
			for (Object id : ids) {

				Object[] tupleList = new Object[columns.size()];

				int i = 0; // @checkstyle:ignore
				for (AggregatePath path : columns.paths()) {
					tupleList[i++] = valueExtractor.apply(id, path);
				}

				parameterValues.add(tupleList);
			}

			parameterSource.addValue(SqlGenerator.IDS_SQL_PARAMETER, parameterValues);

			addSoftDeletePropertyValue(softDeleteProperty, parameterSource);

			return parameterSource;
		});
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.SqlParametersFactory#getIdMapper}
	 */
	private BiFunction<Object, AggregatePath, Object> getIdMapper(@Nullable RelationalPersistentEntity<?> complexId) {

		if (complexId == null) {
			return (id, aggregatePath) -> id;
		}

		return (id, aggregatePath) -> {

			PersistentPropertyAccessor<Object> accessor = complexId.getPropertyAccessor(id);
			return accessor.getProperty(aggregatePath.getRequiredLeafProperty());
		};
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

	private <T> T doWithIdentifiers(Class<?> domainType, IdentifierCallback<T> callback) {

		RelationalPersistentEntity<?> entity = context.getRequiredPersistentEntity(domainType);
		RelationalPersistentProperty idProperty = entity.getRequiredIdProperty();
		RelationalPersistentEntity<?> complexId = context.getPersistentEntity(idProperty);
		AggregatePath.ColumnInfos columns = context.getAggregatePath(entity).getTableInfo().idColumnInfos();

		return callback.doWithIdentifiers(columns, idProperty, complexId);
	}

	interface IdentifierCallback<T> {

		T doWithIdentifiers(AggregatePath.ColumnInfos columns, RelationalPersistentProperty idProperty,
			RelationalPersistentEntity<?> complexId);
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
