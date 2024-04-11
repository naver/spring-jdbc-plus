package com.navercorp.spring.data.jdbc.plus.support.convert;

import static com.navercorp.spring.data.jdbc.plus.support.convert.SqlGenerator.IDS_SQL_PARAMETER;
import static com.navercorp.spring.data.jdbc.plus.support.convert.SqlGenerator.ID_SQL_PARAMETER;
import static com.navercorp.spring.data.jdbc.plus.support.convert.SqlGenerator.VERSION_SQL_PARAMETER;
import static java.util.Objects.requireNonNull;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jdbc.core.convert.DataAccessStrategy;
import org.springframework.data.jdbc.core.convert.DelegatingDataAccessStrategy;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.support.parametersource.SoftDeleteSqlParametersFactory;

public class JdbcPlusDataAccessStrategy extends DelegatingDataAccessStrategy {

	private final RelationalMappingContext context;
	private final NamedParameterJdbcOperations operations;
	private final SqlGeneratorSource sqlGeneratorSource;
	private final SoftDeleteSqlParametersFactory softDeleteSqlParametersFactory;

	public JdbcPlusDataAccessStrategy(
		DataAccessStrategy delegate,
		RelationalMappingContext context,
		NamedParameterJdbcOperations operations,
		SqlGeneratorSource sqlGeneratorSource,
		SoftDeleteSqlParametersFactory softDeleteSqlParametersFactory
	) {
		super(delegate);

		Assert.notNull(context, "RelationalMappingContext must not be null");
		Assert.notNull(operations, "NamedParameterJdbcOperations must not be null");
		Assert.notNull(sqlGeneratorSource, "SqlGeneratorSource must not be null");
		Assert.notNull(softDeleteSqlParametersFactory, "SoftDeleteSqlParameterFactory must not be null");

		this.context = context;
		this.operations = operations;
		this.sqlGeneratorSource = sqlGeneratorSource;
		this.softDeleteSqlParametersFactory = softDeleteSqlParametersFactory;
	}

	@Override
	public void delete(Object rootId, PersistentPropertyPath<RelationalPersistentProperty> propertyPath) {
		RelationalPersistentEntity<?> rootEntity = context.getRequiredPersistentEntity(getBaseType(propertyPath));

		RelationalPersistentProperty referencingProperty = propertyPath.getLeafProperty();
		Assert.notNull(referencingProperty, "No property found matching the PropertyPath " + propertyPath);

		if (!supportsSoftDelete(rootEntity.getType())) {
			super.delete(rootId, propertyPath);
			return;
		}

		String softDelete = sql(rootEntity.getType()).getSoftDeleteById();

		SqlParameterSource parameters = softDeleteSqlParametersFactory.forSoftDeleteById(
			rootId,
			rootEntity.getType(),
			ID_SQL_PARAMETER,
			requireNonNull(getSoftDeleteProperty(rootEntity.getType()))
		);
		operations.update(softDelete, parameters);
	}

	@Override
	public void delete(Iterable<Object> rootIds, PersistentPropertyPath<RelationalPersistentProperty> propertyPath) {
		RelationalPersistentEntity<?> rootEntity = context.getRequiredPersistentEntity(getBaseType(propertyPath));

		RelationalPersistentProperty referencingProperty = propertyPath.getLeafProperty();
		Assert.notNull(referencingProperty, "No property found matching the PropertyPath " + propertyPath);

		if (!supportsSoftDelete(rootEntity.getType())) {
			super.delete(rootIds, propertyPath);
			return;
		}

		String softDelete = sql(rootEntity.getType()).getDeleteByIdIn();

		SqlParameterSource parameters = softDeleteSqlParametersFactory.forSoftDeleteByIds(
			rootIds,
			rootEntity.getType(),
			IDS_SQL_PARAMETER,
			requireNonNull(getSoftDeleteProperty(rootEntity.getType()))
		);
		operations.update(softDelete, parameters);
	}

	@Override
	public void delete(Object id, Class<?> domainType) {
		if (!supportsSoftDelete(domainType)) {
			super.delete(id, domainType);
			return;
		}

		String deleteByIdSql = sql(domainType).getSoftDeleteById();
		SqlParameterSource parameter = softDeleteSqlParametersFactory.forSoftDeleteById(
			id,
			domainType,
			ID_SQL_PARAMETER,
			requireNonNull(getSoftDeleteProperty(domainType))
		);

		operations.update(deleteByIdSql, parameter);
	}

	@Override
	public void delete(Iterable<Object> ids, Class<?> domainType) {
		if (!supportsSoftDelete(domainType)) {
			super.delete(ids, domainType);
			return;
		}

		String softDelete = sql(domainType).getSoftDeleteByIdIn();
		SqlParameterSource parameter = softDeleteSqlParametersFactory.forSoftDeleteByIds(
			ids,
			domainType,
			IDS_SQL_PARAMETER,
			requireNonNull(getSoftDeleteProperty(domainType))
		);

		operations.update(softDelete, parameter);
	}

	@Override
	public <T> void deleteWithVersion(Object id, Class<T> domainType, Number previousVersion) {
		if (!supportsSoftDelete(domainType)) {
			super.deleteWithVersion(id, domainType, previousVersion);
			return;
		}

		SqlParameterSource parameterSource = softDeleteSqlParametersFactory.forSoftDeleteByIdWithVersion(
			id,
			domainType,
			ID_SQL_PARAMETER,
			requireNonNull(getSoftDeleteProperty(domainType)),
			VERSION_SQL_PARAMETER,
			previousVersion
		);

		int affectedRows = operations.update(sql(domainType).getSoftDeleteByIdAndVersion(), parameterSource);

		if (affectedRows == 0) {
			throw new OptimisticLockingFailureException(
				String.format("Optimistic lock exception deleting entity of type %s", domainType.getName()));
		}
	}

	@Override
	public <T> void deleteAll(Class<T> domainType) {
		if (!supportsSoftDelete(domainType)) {
			super.deleteAll(domainType);
			return;
		}

		SqlParameterSource parameterSource = softDeleteSqlParametersFactory.forSoftDeleteAll(
			requireNonNull(getSoftDeleteProperty(domainType))
		);

		operations.update(
			sql(domainType).createSoftDeleteAllSql(null),
			parameterSource
		);
	}

	@Override
	public void deleteAll(PersistentPropertyPath<RelationalPersistentProperty> propertyPath) {
		Class<?> domainType = getBaseType(propertyPath);

		if (!supportsSoftDelete(domainType)) {
			super.deleteAll(domainType);
			return;
		}

		SqlParameterSource parameterSource = softDeleteSqlParametersFactory.forSoftDeleteAll(
			requireNonNull(getSoftDeleteProperty(domainType))
		);

		operations.update(
			sql(domainType).createSoftDeleteAllSql(propertyPath),
			parameterSource
		);
	}

	private boolean supportsSoftDelete(Class<?> domainType) {
		return getSoftDeleteProperty(domainType).exists();
	}

	private SoftDeleteProperty getSoftDeleteProperty(Class<?> domainType) {
		return sql(domainType).getSoftDeleteProperty();
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.DefaultDataAccessStrategy#sql}
	 */
	private SqlGenerator sql(Class<?> domainType) {
		return sqlGeneratorSource.getSqlGenerator(domainType);
	}

	/**
	 * COPY {@link org.springframework.data.jdbc.core.convert.DefaultDataAccessStrategy#getBaseType}
	 */
	private Class<?> getBaseType(PersistentPropertyPath<RelationalPersistentProperty> propertyPath) {

		RelationalPersistentProperty baseProperty = propertyPath.getBaseProperty();

		Assert.notNull(baseProperty, "The base property must not be null");

		return baseProperty.getOwner().getType();
	}
}
