/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.convert.Identifier;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.QueryMapper;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.RenderContextFactory;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.sql.Aliased;
import org.springframework.data.relational.core.sql.AssignValue;
import org.springframework.data.relational.core.sql.Assignments;
import org.springframework.data.relational.core.sql.BindMarker;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Delete;
import org.springframework.data.relational.core.sql.DeleteBuilder;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Expressions;
import org.springframework.data.relational.core.sql.From;
import org.springframework.data.relational.core.sql.Functions;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.Insert;
import org.springframework.data.relational.core.sql.InsertBuilder;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.core.sql.Named;
import org.springframework.data.relational.core.sql.OrderByField;
import org.springframework.data.relational.core.sql.SQL;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder;
import org.springframework.data.relational.core.sql.SelectList;
import org.springframework.data.relational.core.sql.SimpleFunction;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.relational.core.sql.StatementBuilder;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.core.sql.Update;
import org.springframework.data.relational.core.sql.UpdateBuilder;
import org.springframework.data.relational.core.sql.Visitor;
import org.springframework.data.relational.core.sql.render.RenderContext;
import org.springframework.data.relational.core.sql.render.RenderNamingStrategy;
import org.springframework.data.relational.core.sql.render.SqlRenderer;
import org.springframework.data.util.Lazy;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.navercorp.spring.data.jdbc.plus.sql.annotation.SqlFunction;
import com.navercorp.spring.data.jdbc.plus.sql.parametersource.BindParameterNameSanitizer;

/**
 * Generates SQL statements to be used by {@link org.springframework.data.jdbc.repository.support.SimpleJdbcRepository}
 *
 * @author Jens Schauder
 * @author Yoichi Imai
 * @author Bastian Wilhelm
 * @author Oleksandr Kucher
 * @author Mark Paluch
 * @author Tom Hombergs
 * @author Tyler Van Gorder
 * @author Milan Milanov
 * @author Myeonghyeon Lee
 *
 * Copy: org.springframework.data.jdbc.core.convert.SqlGenerator
 * VERIFIED: 91ddda0c3f97858e75cbf44ee23054559dd0daeb
 */
@SuppressWarnings("checkstyle:linelength")
class SqlGenerator {

	static final SqlIdentifier VERSION_SQL_PARAMETER = SqlIdentifier.unquoted("___oldOptimisticLockingVersion");
	static final SqlIdentifier ID_SQL_PARAMETER = SqlIdentifier.unquoted("id");
	static final SqlIdentifier IDS_SQL_PARAMETER = SqlIdentifier.unquoted("ids");
	static final SqlIdentifier ROOT_ID_PARAMETER = SqlIdentifier.unquoted("rootId");

	private final RelationalPersistentEntity<?> entity;
	private final MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext;
	private final RenderContext renderContext;

	private final SqlContexts sqlContext;
	private final SqlRenderer sqlRenderer;
	private final Columns columns;

	private final Lazy<String> findOneSql = Lazy.of(this::createFindOneSql);
	private final Lazy<String> findAllSql = Lazy.of(this::createFindAllSql);
	private final Lazy<String> findAllInListSql = Lazy.of(this::createFindAllInListSql);

	private final Lazy<String> existsSql = Lazy.of(this::createExistsSql);
	private final Lazy<String> countSql = Lazy.of(this::createCountSql);

	private final Lazy<String> updateSql = Lazy.of(this::createUpdateSql);
	private final Lazy<String> updateWithVersionSql = Lazy.of(this::createUpdateWithVersionSql);

	private final Lazy<String> upsertSql = Lazy.of(this::createUpsertSql);

	private final Lazy<String> deleteByIdSql = Lazy.of(this::createDeleteByIdSql);
	private final Lazy<String> deleteByIdInSql = Lazy.of(this::createDeleteByIdInSql);
	private final Lazy<String> deleteByIdAndVersionSql = Lazy.of(this::createDeleteByIdAndVersionSql);
	private final Lazy<String> deleteByListSql = Lazy.of(this::createDeleteByListSql);
	private final QueryMapper queryMapper;
	private final Dialect dialect;

	/**
	 * Create a new {@link SqlGenerator} given {@link RelationalMappingContext}
	 * and {@link RelationalPersistentEntity}.
	 *
	 * @param mappingContext must not be {@literal null}.
	 * @param converter must not be {@literal null}.
	 * @param entity         must not be {@literal null}.
	 * @param dialect must not be {@literal null}.
	 */
	SqlGenerator(
		RelationalMappingContext mappingContext,
		JdbcConverter converter,
		RelationalPersistentEntity<?> entity,
		Dialect dialect) {

		this.mappingContext = mappingContext;
		this.entity = entity;
		this.sqlContext = new SqlContext(entity);
		this.renderContext = new RenderContextFactory(dialect).createRenderContext();
		this.sqlRenderer = SqlRenderer.create(renderContext);
		this.columns = new Columns(entity, mappingContext, converter);
		this.queryMapper = new QueryMapper(dialect, converter);
		this.dialect = dialect;
	}

	/**
	 * DIFF
	 * Additional constructor for internal extension.
	 */
	SqlGenerator(
		RelationalMappingContext mappingContext,
		JdbcConverter converter,
		RelationalPersistentEntity<?> entity,
		Dialect dialect,
		SqlContexts sqlContexts) {

		this.mappingContext = mappingContext;
		this.entity = entity;
		this.columns = new Columns(entity, mappingContext, converter);
		this.sqlRenderer = SqlRenderer.create(new RenderContextFactory(dialect).createRenderContext());
		this.renderContext = new RenderContextFactory(dialect).createRenderContext();
		this.sqlContext = sqlContexts;
		this.queryMapper = new QueryMapper(dialect, converter);
		this.dialect = dialect;
	}

	/**
	 * Construct an IN-condition based on a {@link Select Sub-Select}
	 * which selects the ids (or stand-ins for ids) of the
	 * given {@literal path} to those that reference the root entities specified
	 * by the {@literal rootCondition}.
	 *
	 * @param path          specifies the table and id to select
	 * @param rootCondition the condition on the root of the path determining what to select
	 * @param filterColumn  the column to apply the IN-condition to.
	 * @return the IN condition
	 */
	private Condition getSubselectCondition(PersistentPropertyPathExtension path,
		Function<Column, Condition> rootCondition, Column filterColumn) {

		PersistentPropertyPathExtension parentPath = path.getParentPath();

		if (!parentPath.hasIdProperty()) {
			if (parentPath.getLength() > 1) {
				return getSubselectCondition(parentPath, rootCondition, filterColumn);
			}
			return rootCondition.apply(filterColumn);
		}

		Table subSelectTable = Table.create(parentPath.getQualifiedTableName());
		Column idColumn = subSelectTable.column(parentPath.getIdColumnName());
		Column selectFilterColumn = subSelectTable.column(parentPath.getEffectiveIdColumnName());

		Condition innerCondition;

		if (parentPath.getLength() == 1) { // if the parent is the root of the path

			// apply the rootCondition
			innerCondition = rootCondition.apply(selectFilterColumn);
		} else {

			// otherwise, we need another layer of subselect
			innerCondition = getSubselectCondition(parentPath, rootCondition, selectFilterColumn);
		}

		Select select = Select.builder() //
			.select(idColumn) //
			.from(subSelectTable) //
			.where(innerCondition).build();

		return filterColumn.in(select);
	}

	private BindMarker getBindMarker(SqlIdentifier columnName) {
		return SQL.bindMarker(":" + BindParameterNameSanitizer.sanitize(renderReference(columnName)));
	}

	/**
	 * Returns a query for selecting all simple properties of an entity,
	 * including those for one-to-one relationships.
	 * Results are filtered using an {@code IN}-clause on the id column.
	 *
	 * @return a SQL statement. Guaranteed to be not {@code null}.
	 */
	String getFindAllInList() {
		return findAllInListSql.get();
	}

	/**
	 * Returns a query for selecting all simple properties of an entity,
	 * including those for one-to-one relationships.
	 *
	 * @return a SQL statement. Guaranteed to be not {@code null}.
	 */
	String getFindAll() {
		return findAllSql.get();
	}

	/**
	 * Returns a query for selecting all simple properties of an entity,
	 * including those for one-to-one relationships,
	 * sorted by the given parameter.
	 *
	 * @return a SQL statement. Guaranteed to be not {@code null}.
	 */
	String getFindAll(Sort sort) {
		return render(selectBuilder(Collections.emptyList(), sort, Pageable.unpaged()).build());
	}

	/**
	 * Returns a query for selecting all simple properties of an entity,
	 * including those for one-to-one relationships,
	 * paged and sorted by the given parameter.
	 *
	 * @return a SQL statement. Guaranteed to be not {@code null}.
	 */
	String getFindAll(Pageable pageable) {
		return render(selectBuilder(Collections.emptyList(), pageable.getSort(), pageable).build());
	}

	/**
	 * Returns a query for selecting all simple properties of an entity, including those for one-to-one relationships.
	 * Results are limited to those rows referencing some parent entity. This is used to select values for a complex
	 * property ({@link Set}, {@link Map} ...) based on a referencing entity.
	 *
	 * @param parentIdentifier name of the column of the FK back to the referencing entity.
	 * @param propertyPath used to determine if the property is ordered and if there is a key column.
	 * @return a SQL String.
	 * @since 3.0
	 */
	String getFindAllByProperty(Identifier parentIdentifier,
		PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath) {

		Assert.notNull(parentIdentifier, "identifier must not be null");
		Assert.notNull(propertyPath, "propertyPath must not be null");

		PersistentPropertyPathExtension path = new PersistentPropertyPathExtension(mappingContext, propertyPath);

		return getFindAllByProperty(parentIdentifier, path.getQualifierColumn(), path.isOrdered());
	}

	/**
	 * Returns a query for selecting all simple properties of an entity,
	 * including those for one-to-one relationships.
	 * Results are limited to those rows referencing some other entity using the column specified by
	 * {@literal columnName}. This is used to select values
	 * for a complex property ({@link Set}, {@link Map} ...) based on a referencing entity.
	 *
	 * @param parentIdentifier name of the column of the FK back to the referencing entity.
	 * @param keyColumn        if the property is of type {@link Map} this column contains the map key.
	 * @param ordered          whether the SQL statement should include an ORDER BY for the keyColumn.
	 *                            If this is {@code true}, the keyColumn must not be {@code null}.
	 * @return a SQL String.
	 */
	String getFindAllByProperty(Identifier parentIdentifier, @Nullable SqlIdentifier keyColumn, boolean ordered) {

		Assert.isTrue(keyColumn != null || !ordered,
			"If the SQL statement should be ordered a keyColumn to order by must be provided");

		Table table = getTable();

		SelectBuilder.SelectWhere builder = selectBuilder(
			keyColumn == null
				? Collections.emptyList()
				: Collections.singleton(keyColumn)
		);

		Condition condition = buildConditionForBackReference(parentIdentifier, table);
		SelectBuilder.SelectWhereAndOr withWhereClause = builder.where(condition);

		Select select = ordered //
			? withWhereClause.orderBy(table.column(keyColumn).as(keyColumn)).build() //
			: withWhereClause.build();

		return render(select);
	}

	private Condition buildConditionForBackReference(Identifier parentIdentifier, Table table) {

		Condition condition = null;
		for (SqlIdentifier backReferenceColumn : parentIdentifier.toMap().keySet()) {
			Assert.isTrue(!SqlIdentifier.EMPTY.equals(backReferenceColumn),
				"An empty SqlIdentifier can't be used in condition. Make sure that all composite primary keys are defined in the query"); // @checkstyle:ignoreLength

			Condition newCondition = table.column(backReferenceColumn)
				.isEqualTo(getBindMarker(backReferenceColumn));
			condition = condition == null ? newCondition : condition.and(newCondition);
		}

		Assert.state(condition != null, "We need at least one condition");

		return condition;
	}

	/**
	 * Create a {@code SELECT COUNT(id) FROM … WHERE :id = …} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getExists() {
		return existsSql.get();
	}

	/**
	 * Create a {@code SELECT … FROM … WHERE :id = …} statement.
	 *
	 * @return
	 */
	String getFindOne() {
		return findOneSql.get();
	}

	/**
	 * Create a {@code SELECT count(id) FROM … WHERE :id = … (LOCK CLAUSE)} statement.
	 *
	 * @param lockMode Lock clause mode.
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getAcquireLockById(LockMode lockMode) {
		return this.createAcquireLockById(lockMode);
	}

	/**
	 * Create a {@code SELECT count(id) FROM … (LOCK CLAUSE)} statement.
	 *
	 * @param lockMode Lock clause mode.
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getAcquireLockAll(LockMode lockMode) {
		return this.createAcquireLockAll(lockMode);
	}

	/**
	 * Create a {@code INSERT INTO … (…) VALUES(…)} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getInsert(Set<SqlIdentifier> additionalColumns) {
		return createInsertSql(additionalColumns);
	}

	/**
	 * Create a {@code UPDATE … SET …} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getUpdate() {
		return updateSql.get();
	}

	/**
	 * Create a {@code UPDATE … SET … WHERE ID = :id
	 * and VERSION_COLUMN = :___oldOptimisticLockingVersion } statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getUpdateWithVersion() {
		return updateWithVersionSql.get();
	}

	/**
	 * Create a {@code INSERT INTO … SET ID = :id,… ON DUPLICATE KEY UPDATE …} statement.
	 * ON DUPLICATE KEY UPDATE of this statement supports MySQL Dialect.
	 * Don't use it as it provides an experimental method.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getUpsert() {
		return upsertSql.get();
	}

	/**
	 * Create a {@code SELECT COUNT(*) FROM …} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getCount() {
		return countSql.get();
	}

	/**
	 * Create a {@code DELETE FROM … WHERE :id = …} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getDeleteById() {
		return deleteByIdSql.get();
	}

	/**
	 * Create a {@code DELETE FROM … WHERE :id IN …} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getDeleteByIdIn() {
		return deleteByIdInSql.get();
	}

	/**
	 * Create a {@code DELETE FROM … WHERE :id = …
	 * and :___oldOptimisticLockingVersion = ...} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getDeleteByIdAndVersion() {
		return deleteByIdAndVersionSql.get();
	}

	/**
	 * Create a {@code DELETE FROM … WHERE :ids in (…)} statement.
	 *
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String getDeleteByList() {
		return deleteByListSql.get();
	}

	/**
	 * Create a {@code DELETE} query and optionally filter by {@link PersistentPropertyPath}.
	 *
	 * @param path can be {@literal null}.
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String createDeleteAllSql(
		@Nullable PersistentPropertyPath<RelationalPersistentProperty> path) {

		Table table = getDmlTable();

		DeleteBuilder.DeleteWhere deleteAll = Delete.builder().from(table);

		if (path == null) {
			return render(deleteAll.build());
		}

		return createDeleteByPathAndCriteria(
			new PersistentPropertyPathExtension(mappingContext, path), Column::isNotNull);
	}

	/**
	 * Create a {@code DELETE} query and filter by {@link PersistentPropertyPath} using {@code WHERE} with the {@code =}
	 * operator.
	 * @param path must not be {@literal null}.
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String createDeleteByPath(PersistentPropertyPath<RelationalPersistentProperty> path) {
		return createDeleteByPathAndCriteria(new PersistentPropertyPathExtension(mappingContext, path),
			filterColumn -> filterColumn.isEqualTo(getBindMarker(ROOT_ID_PARAMETER)));
	}

	/**
	 * Create a {@code DELETE} query and filter by {@link PersistentPropertyPath} using {@code WHERE} with the {@code IN}
	 * operator.
	 *
	 * @param path must not be {@literal null}.
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	String createDeleteInByPath(PersistentPropertyPath<RelationalPersistentProperty> path) {

		return createDeleteByPathAndCriteria(new PersistentPropertyPathExtension(mappingContext, path),
			filterColumn -> filterColumn.in(getBindMarker(IDS_SQL_PARAMETER)));
	}

	private String createFindOneSql() {

		Select select = selectBuilder().where(getIdColumn().isEqualTo(getBindMarker(ID_SQL_PARAMETER))) //
			.build();

		return render(select);
	}

	private String createAcquireLockById(LockMode lockMode) {

		Table table = this.getTable();

		Select select = StatementBuilder //
			.select(getIdColumn()) //
			.from(table) //
			.where(getIdColumn().isEqualTo(getBindMarker(ID_SQL_PARAMETER))) //
			.lock(lockMode) //
			.build();

		return render(select);
	}

	private String createAcquireLockAll(LockMode lockMode) {

		Table table = this.getTable();

		Select select = StatementBuilder //
			.select(getIdColumn()) //
			.from(table) //
			.lock(lockMode) //
			.build();

		return render(select);
	}

	private String createFindAllSql() {
		return render(selectBuilder().build());
	}

	private SelectBuilder.SelectWhere selectBuilder() {
		return selectBuilder(Collections.emptyList());
	}

	private SelectBuilder.SelectWhere selectBuilder(Collection<SqlIdentifier> keyColumns) {

		Table table = getTable();

		List<Expression> columnExpressions = new ArrayList<>();

		List<Join> joinTables = new ArrayList<>();
		for (PersistentPropertyPath<RelationalPersistentProperty> path : mappingContext
			.findPersistentPropertyPaths(entity.getType(), p -> true)) {

			PersistentPropertyPathExtension extPath =
				new PersistentPropertyPathExtension(mappingContext, path);

			// add a join if necessary
			Join join = getJoin(extPath);
			if (join != null) {
				joinTables.add(join);
			}

			Column column = getColumn(extPath);
			if (column != null) {
				columnExpressions.add(getColumnExpression(extPath, column));
			}
		}

		for (SqlIdentifier keyColumn : keyColumns) {
			columnExpressions.add(table.column(keyColumn).as(keyColumn));
		}

		SelectBuilder.SelectAndFrom selectBuilder = StatementBuilder.select(columnExpressions);
		SelectBuilder.SelectJoin baseSelect = selectBuilder.from(table);

		for (Join join : joinTables) {
			baseSelect = baseSelect.leftOuterJoin(join.joinTable).on(join.joinColumn).equals(join.parentId);
		}

		return (SelectBuilder.SelectWhere)baseSelect;
	}

	private SelectBuilder.SelectOrdered selectBuilder(Collection<SqlIdentifier> keyColumns, Sort sort,
		Pageable pageable) {

		SelectBuilder.SelectOrdered sortable = this.selectBuilder(keyColumns);
		sortable = applyPagination(pageable, sortable);
		return sortable.orderBy(extractOrderByFields(sort));
	}

	private SelectBuilder.SelectOrdered applyPagination(
		Pageable pageable, SelectBuilder.SelectOrdered select) {

		if (!pageable.isPaged()) {
			return select;
		}

		Assert.isTrue(select instanceof SelectBuilder.SelectLimitOffset,
			() -> String.format("Can't apply limit clause to statement of type %s", select.getClass()));

		SelectBuilder.SelectLimitOffset limitable = (SelectBuilder.SelectLimitOffset)select;
		SelectBuilder.SelectLimitOffset limitResult = limitable.limitOffset(
			pageable.getPageSize(), pageable.getOffset());

		Assert.state(limitResult instanceof SelectBuilder.SelectOrdered, String.format(
			"The result of applying the limit-clause must be of type SelectOrdered "
				+ "in order to apply the order-by-clause but is of type %s",
			select.getClass()));

		return (SelectBuilder.SelectOrdered)limitResult;
	}

	/**
	 * DIFF
	 * Additional custom method for {@link SqlProvider}.
	 */
	String selectColumns() {
		List<Expression> columnExpressions = new ArrayList<>();
		for (PersistentPropertyPath<RelationalPersistentProperty> path : mappingContext
			.findPersistentPropertyPaths(entity.getType(), p -> true)) {

			PersistentPropertyPathExtension extPath =
				new PersistentPropertyPathExtension(mappingContext, path);
			Column column = getColumn(extPath);
			if (column != null) {
				columnExpressions.add(getColumnExpression(extPath, column));
			}
		}

		return this.render(new SimpleSelect(columnExpressions));
	}

	/**
	 * DIFF
	 * Additional custom method for {@link SqlProvider}.
	 */
	String selectFrom() {
		// Row DTO
		if (!entity.hasIdProperty()) {
			return this.selectColumns();
		}

		Table table = getTable();

		List<Expression> columnExpressions = new ArrayList<>();

		List<Join> joinTables = new ArrayList<>();
		for (PersistentPropertyPath<RelationalPersistentProperty> path : mappingContext
			.findPersistentPropertyPaths(entity.getType(), p -> true)) {

			PersistentPropertyPathExtension extPath =
				new PersistentPropertyPathExtension(mappingContext, path);

			// add a join if necessary
			Join join = getJoin(extPath);
			if (join != null) {
				joinTables.add(join);
			}

			Column column = getColumn(extPath);
			if (column != null) {
				columnExpressions.add(getColumnExpression(extPath, column));
			}
		}

		SelectBuilder.SelectAndFrom selectBuilder = StatementBuilder.select(columnExpressions);
		SelectBuilder.SelectJoin baseSelect = selectBuilder.from(table);

		for (Join join : joinTables) {
			baseSelect = baseSelect.leftOuterJoin(join.joinTable)
				.on(join.joinColumn).equals(join.parentId);
		}

		return this.render(baseSelect.build());
	}

	/**
	 * DIFF
	 * Additional custom method for {@link SqlProvider}.
	 */
	String selectAggregateFrom() {
		Table table = getTable();

		List<Expression> columnExpressions = new ArrayList<>();

		List<Join> joinTables = new ArrayList<>();
		for (PersistentPropertyPath<RelationalPersistentProperty> path : mappingContext
			.findPersistentPropertyPaths(entity.getType(), p -> true)) {

			PersistentPropertyPathExtension extPath =
				new PersistentPropertyPathExtension(mappingContext, path);

			// add a join if necessary
			if (extPath.isEntity() && !extPath.isEmbedded()) {
				Table currentTable = sqlContext.getTable(extPath);

				PersistentPropertyPathExtension idDefiningParentPath =
					extPath.getIdDefiningParentPath();
				Table parentTable = sqlContext.getTable(idDefiningParentPath);

				joinTables.add(new Join( //
					currentTable, //
					currentTable.column(extPath.getReverseColumnName()),
					parentTable.column(idDefiningParentPath.getIdColumnName())));
			}

			Column column;
			if (extPath.isEmbedded()) {
				column = null;
			} else if (extPath.isEntity()) {
				if (extPath.isQualified() //
					|| extPath.isCollectionLike() //
					|| extPath.hasIdProperty() //
				) {
					column = null;
				} else {
					column = sqlContext.getReverseColumn(extPath);
				}
			} else {
				column = sqlContext.getColumn(extPath);
			}

			if (column != null) {
				columnExpressions.add(column);
			}
		}

		SelectBuilder.SelectAndFrom selectBuilder = StatementBuilder.select(columnExpressions);
		SelectBuilder.SelectJoin baseSelect = selectBuilder.from(table);

		for (Join join : joinTables) {
			baseSelect = baseSelect.leftOuterJoin(join.joinTable)
				.on(join.joinColumn).equals(join.parentId);
		}

		return this.render(baseSelect.build());
	}

	/**
	 * Create a {@link Column} for {@link PersistentPropertyPathExtension}.
	 *
	 * @param path the path to the column in question.
	 * @return the statement as a {@link String}. Guaranteed to be not {@literal null}.
	 */
	@Nullable
	Column getColumn(PersistentPropertyPathExtension path) {

		// an embedded itself doesn't give a column, its members will though.
		// if there is a collection or map on the path it won't get selected at all,
		// but it will get loaded with a separate
		// select
		// only the parent path is considered in order to handle arrays
		// that get stored as BINARY properly
		if (path.isEmbedded() || path.getParentPath().isMultiValued()) {
			return null;
		}

		if (path.isEntity()) {

			// Simple entities without id include there backreference as a synthetic id
			// in order to distinguish null entities
			// from entities with only null values.

			if (path.isQualified() //
				|| path.isCollectionLike() //
				|| path.hasIdProperty() //
			) {
				return null;
			}

			return sqlContext.getReverseColumn(path);
		}

		return sqlContext.getColumn(path);
	}

	@Nullable
	Join getJoin(PersistentPropertyPathExtension path) {

		if (!path.isEntity() || path.isEmbedded() || path.isMultiValued()) {
			return null;
		}

		Table currentTable = sqlContext.getTable(path);

		PersistentPropertyPathExtension idDefiningParentPath = path.getIdDefiningParentPath();
		Table parentTable = sqlContext.getTable(idDefiningParentPath);

		return new Join( //
			currentTable, //
			currentTable.column(path.getReverseColumnName()), //
			parentTable.column(idDefiningParentPath.getIdColumnName()) //
		);
	}

	private String createFindAllInListSql() {

		Select select = selectBuilder().where(getIdColumn()
			.in(getBindMarker(IDS_SQL_PARAMETER))).build();

		return render(select);
	}

	private String createExistsSql() {

		Table table = getTable();

		Select select = StatementBuilder //
			.select(Functions.count(getIdColumn())) //
			.from(table) //
			.where(getIdColumn().isEqualTo(getBindMarker(ID_SQL_PARAMETER))) //
			.build();

		return render(select);
	}

	private String createCountSql() {

		Table table = getTable();

		Select select = StatementBuilder //
			.select(Functions.count(Expressions.asterisk())) //
			.from(table) //
			.build();

		return render(select);
	}

	private String createInsertSql(Set<SqlIdentifier> additionalColumns) {

		Table table = getDmlTable();

		Set<SqlIdentifier> columnNamesForInsert =
			new TreeSet<>(Comparator.comparing(SqlIdentifier::getReference));
		columnNamesForInsert.addAll(columns.getInsertableColumns());
		columnNamesForInsert.addAll(additionalColumns);

		InsertBuilder.InsertIntoColumnsAndValuesWithBuild insert = Insert.builder().into(table);

		for (SqlIdentifier cn : columnNamesForInsert) {
			insert = insert.column(table.column(cn));
		}

		if (columnNamesForInsert.isEmpty()) {
			return render(insert.build());
		}

		InsertBuilder.InsertValuesWithBuild insertWithValues = null;
		for (SqlIdentifier cn : columnNamesForInsert) {
			insertWithValues = (insertWithValues == null
				? insert
				: insertWithValues).values(getBindMarker(cn));
		}

		return render(insertWithValues.build());
	}

	private String createUpdateSql() {
		return render(createBaseUpdate().build());
	}

	private String createUpdateWithVersionSql() {

		Update update = createBaseUpdate() //
			.and(getDmlVersionColumn().isEqualTo(getBindMarker(VERSION_SQL_PARAMETER))) //
			.build();

		return render(update);
	}

	private UpdateBuilder.UpdateWhereAndOr createBaseUpdate() {

		Table table = getDmlTable();

		List<AssignValue> assignments = columns.getUpdatableColumns() //
			.stream() //
			.map(columnName -> Assignments.value( //
				table.column(columnName), //
				getBindMarker(columnName))) //
			.toList();

		return Update.builder() //
			.table(table) //
			.set(assignments) //
			.where(getDmlIdColumn().isEqualTo(getBindMarker(entity.getIdColumn())));
	}

	private String createUpsertSql() {
		Table table = getDmlTable();
		RenderNamingStrategy namingStrategy = this.renderContext.getNamingStrategy();
		IdentifierProcessing identifierProcessing = this.renderContext.getIdentifierProcessing();

		String tableName = namingStrategy.getName(table).toSql(identifierProcessing);
		String insertableAssignmentsSql = Stream.concat(
				columns.idColumnNames.stream(),
				columns.getInsertableColumns().stream()
			)
			.map(columnName -> {
				String name = namingStrategy.getName(table.column(columnName)).toSql(identifierProcessing);
				String bindMarkerName = ((Named)getBindMarker(columnName)).getName().toSql(identifierProcessing);
				return name + " = " + bindMarkerName;
			})
			.collect(Collectors.joining(", "));
		String updatableAssignmentsSql = columns.getUpdatableColumns().stream()
			.map(columnName -> {
				String name = namingStrategy.getName(table.column(columnName)).toSql(identifierProcessing);
				String bindMarkerName = ((Named)getBindMarker(columnName)).getName().toSql(identifierProcessing);
				return name + " = " + bindMarkerName;
			})
			.collect(Collectors.joining(", "));

		return "INSERT INTO " + tableName + " SET " + insertableAssignmentsSql
			+ " ON DUPLICATE KEY UPDATE " + updatableAssignmentsSql;
	}

	private String createDeleteByIdSql() {
		return render(createBaseDeleteById(getDmlTable()).build());
	}

	private String createDeleteByIdInSql() {
		return render(createBaseDeleteByIdIn(getTable()).build());
	}

	private String createDeleteByIdAndVersionSql() {

		Delete delete = createBaseDeleteById(getDmlTable()) //
			.and(getDmlVersionColumn().isEqualTo(
				getBindMarker(VERSION_SQL_PARAMETER))) //
			.build();

		return render(delete);
	}

	private DeleteBuilder.DeleteWhereAndOr createBaseDeleteById(Table table) {
		return Delete.builder().from(table) //
			.where(getDmlIdColumn().isEqualTo(getBindMarker(ID_SQL_PARAMETER)));
	}

	private DeleteBuilder.DeleteWhereAndOr createBaseDeleteByIdIn(Table table) {

		return Delete.builder().from(table) //
			.where(getDmlIdColumn().in(getBindMarker(IDS_SQL_PARAMETER)));
	}

	private String createDeleteByPathAndCriteria(PersistentPropertyPathExtension path,
		Function<Column, Condition> rootCondition) {

		Table table = Table.create(path.getQualifiedTableName());

		DeleteBuilder.DeleteWhere builder = Delete.builder() //
			.from(table);
		Delete delete;

		Column filterColumn = table.column(path.getReverseColumnName());

		if (path.getLength() == 1) {

			delete = builder //
				.where(rootCondition.apply(filterColumn)) //
				.build();
		} else {

			Condition condition = getSubselectCondition(path, rootCondition, filterColumn);
			delete = builder.where(condition).build();
		}

		return render(delete);
	}

	private String createDeleteByListSql() {

		Table table = getDmlTable();

		Delete delete = Delete.builder() //
			.from(table) //
			.where(getDmlIdColumn().in(getBindMarker(IDS_SQL_PARAMETER))) //
			.build();

		return render(delete);
	}

	private String render(Select select) {
		return this.sqlRenderer.render(select);
	}

	private String render(Insert insert) {
		return this.sqlRenderer.render(insert);
	}

	private String render(Update update) {
		return this.sqlRenderer.render(update);
	}

	private String render(Delete delete) {
		return this.sqlRenderer.render(delete);
	}

	private Table getTable() {
		return sqlContext.getTable();
	}

	// DML Table should not be Aliased Table
	private Table getDmlTable() {
		Table table = sqlContext.getTable();
		if (table instanceof Aliased) {
			table = Table.create(table.getName());
		}
		return table;
	}

	private Column getIdColumn() {
		return sqlContext.getIdColumn();
	}

	private Column getDmlIdColumn() {
		return sqlContext.getDmlIdColumn();
	}

	private Column getVersionColumn() {
		return sqlContext.getVersionColumn();
	}

	private Column getDmlVersionColumn() {
		return sqlContext.getDmlVersionColumn();
	}

	private String renderReference(SqlIdentifier identifier) {
		return identifier.getReference(renderContext.getIdentifierProcessing());
	}

	private List<OrderByField> extractOrderByFields(Sort sort) {
		return sort.stream() //
			.map(this::orderToOrderByField) //
			.collect(Collectors.toList());
	}

	private OrderByField orderToOrderByField(Sort.Order order) {

		SqlIdentifier columnName = getColumnNameToSortBy(order);
		Column column = Column.create(columnName, this.getTable());
		return OrderByField.from(column, order.getDirection()).withNullHandling(order.getNullHandling());
	}

	private SqlIdentifier getColumnNameToSortBy(Sort.Order order) {

		RelationalPersistentProperty propertyToSortBy = entity.getPersistentProperty(order.getProperty());
		if (propertyToSortBy != null) {
			return propertyToSortBy.getColumnName();
		}

		PersistentPropertyPath<RelationalPersistentProperty> persistentPropertyPath = mappingContext
			.getPersistentPropertyPath(order.getProperty(), entity.getType());


		propertyToSortBy = persistentPropertyPath.getBaseProperty();

		Assert.state(propertyToSortBy != null && propertyToSortBy.isEmbedded(), () -> String.format( //
			"Specified sorting property '%s' is expected to " + //
				"be the property, named '%s', of embedded entity '%s', but field '%s' is " + //
				"not marked with @Embedded", //
			order.getProperty(), //
			extractFieldNameFromEmbeddedProperty(order), //
			extractEmbeddedPropertyName(order), //
			extractEmbeddedPropertyName(order) //
		));

		RelationalPersistentEntity<?> embeddedEntity = mappingContext
			.getRequiredPersistentEntity(propertyToSortBy.getType());
		return embeddedEntity.getRequiredPersistentProperty(extractFieldNameFromEmbeddedProperty(order)).getColumnName(); // @checkstyle:ignoreLength
	}

	public String extractEmbeddedPropertyName(Sort.Order order) {
		return order.getProperty().substring(0, order.getProperty().indexOf("."));
	}

	public String extractFieldNameFromEmbeddedProperty(Sort.Order order) {
		return order.getProperty().substring(order.getProperty().indexOf(".") + 1);
	}

	/**
	 * Constructs a single sql query that performs select based on the provided query. Additional the bindings for the
	 * where clause are stored after execution into the <code>parameterSource</code>
	 *
	 * @param query the query to base the select on. Must not be null
	 * @param parameterSource the source for holding the bindings
	 * @return a non null query string.
	 */
	public String selectByQuery(Query query, MapSqlParameterSource parameterSource) {

		Assert.notNull(parameterSource, "parameterSource must not be null");

		SelectBuilder.SelectWhere selectBuilder = selectBuilder();

		Select select = applyQueryOnSelect(query, parameterSource, selectBuilder) //
			.build();

		return render(select);
	}

	/**
	 * Constructs a single sql query that performs select based on the provided query and pagination information.
	 * Additional the bindings for the where clause are stored after execution into the <code>parameterSource</code>
	 *
	 * @param query the query to base the select on. Must not be null.
	 * @param pageable the pageable to perform on the select.
	 * @param parameterSource the source for holding the bindings.
	 * @return a non null query string.
	 */
	public String selectByQuery(Query query, MapSqlParameterSource parameterSource, Pageable pageable) {

		Assert.notNull(parameterSource, "parameterSource must not be null");

		SelectBuilder.SelectWhere selectBuilder = selectBuilder();

		// first apply query and then pagination. This means possible query sorting and limiting might be overwritten by the
		// pagination. This is desired.
		SelectBuilder.SelectOrdered selectOrdered = applyQueryOnSelect(query, parameterSource, selectBuilder);
		selectOrdered = applyPagination(pageable, selectOrdered);
		selectOrdered = selectOrdered.orderBy(extractOrderByFields(pageable.getSort()));

		Select select = selectOrdered.build();
		return render(select);
	}

	/**
	 * Constructs a single sql query that performs select count based on the provided query for checking existence.
	 * Additional the bindings for the where clause are stored after execution into the <code>parameterSource</code>
	 *
	 * @param query the query to base the select on. Must not be null
	 * @param parameterSource the source for holding the bindings
	 * @return a non null query string.
	 */
	public String existsByQuery(Query query, MapSqlParameterSource parameterSource) {

		SelectBuilder.SelectJoin baseSelect = getExistsSelect();

		Select select = applyQueryOnSelect(query, parameterSource, (SelectBuilder.SelectWhere)baseSelect) //
			.build();

		return render(select);
	}

	/**
	 * Constructs a single sql query that performs select count based on the provided query. Additional the bindings for
	 * the where clause are stored after execution into the <code>parameterSource</code>
	 *
	 * @param query the query to base the select on. Must not be null
	 * @param parameterSource the source for holding the bindings
	 * @return a non null query string.
	 */
	public String countByQuery(Query query, MapSqlParameterSource parameterSource) {

		Expression countExpression = Expressions.just("1");
		SelectBuilder.SelectJoin baseSelect = getSelectCountWithExpression(countExpression);

		Select select = applyQueryOnSelect(query, parameterSource, (SelectBuilder.SelectWhere)baseSelect) //
			.build();

		return render(select);
	}

	/**
	 * Generates a {@link org.springframework.data.relational.core.sql.SelectBuilder.SelectJoin} with a
	 * <code>COUNT(...)</code> where the <code>countExpressions</code> are the parameters of the count.
	 *
	 * @return a non-null {@link org.springframework.data.relational.core.sql.SelectBuilder.SelectJoin} that joins all the
	 *         columns and has only a count in the projection of the select.
	 */
	private SelectBuilder.SelectJoin getExistsSelect() {

		Table table = getTable();

		SelectBuilder.SelectJoin baseSelect = StatementBuilder //
			.select(dialect.getExistsFunction()) //
			.from(table);

		// add possible joins
		for (PersistentPropertyPath<RelationalPersistentProperty> path : mappingContext
			.findPersistentPropertyPaths(entity.getType(), p -> true)) {

			PersistentPropertyPathExtension extPath = new PersistentPropertyPathExtension(mappingContext, path);

			// add a join if necessary
			Join join = getJoin(extPath);
			if (join != null) {
				baseSelect = baseSelect.leftOuterJoin(join.joinTable).on(join.joinColumn).equals(join.parentId);
			}
		}
		return baseSelect;
	}

	/**
	 * Generates a {@link org.springframework.data.relational.core.sql.SelectBuilder.SelectJoin} with a
	 * <code>COUNT(...)</code> where the <code>countExpressions</code> are the parameters of the count.
	 *
	 * @param countExpressions the expression to use as count parameter.
	 * @return a non-null {@link org.springframework.data.relational.core.sql.SelectBuilder.SelectJoin} that joins all the
	 *         columns and has only a count in the projection of the select.
	 */
	private SelectBuilder.SelectJoin getSelectCountWithExpression(Expression... countExpressions) {

		Assert.notNull(countExpressions, "countExpressions must not be null");
		Assert.state(countExpressions.length >= 1, "countExpressions must contain at least one expression");

		Table table = getTable();

		SelectBuilder.SelectJoin baseSelect = StatementBuilder //
			.select(Functions.count(countExpressions)) //
			.from(table);

		// add possible joins
		for (PersistentPropertyPath<RelationalPersistentProperty> path : mappingContext
			.findPersistentPropertyPaths(entity.getType(), p -> true)) {

			PersistentPropertyPathExtension extPath = new PersistentPropertyPathExtension(mappingContext, path);

			// add a join if necessary
			Join join = getJoin(extPath);
			if (join != null) {
				baseSelect = baseSelect.leftOuterJoin(join.joinTable).on(join.joinColumn).equals(join.parentId);
			}
		}
		return baseSelect;
	}

	private SelectBuilder.SelectOrdered applyQueryOnSelect(Query query, MapSqlParameterSource parameterSource,
		SelectBuilder.SelectWhere selectBuilder) {

		Table table = Table.create(this.entity.getQualifiedTableName());

		SelectBuilder.SelectOrdered selectOrdered = query //
			.getCriteria() //
			.map(item -> this.applyCriteria(item, selectBuilder, parameterSource, table)) //
			.orElse(selectBuilder);

		if (query.isSorted()) {
			List<OrderByField> sort = this.queryMapper.getMappedSort(table, query.getSort(), entity);
			selectOrdered = selectBuilder.orderBy(sort);
		}

		SelectBuilder.SelectLimitOffset limitable = (SelectBuilder.SelectLimitOffset)selectOrdered;

		if (query.getLimit() > 0) {
			limitable = limitable.limit(query.getLimit());
		}

		if (query.getOffset() > 0) {
			limitable = limitable.offset(query.getOffset());
		}
		return (SelectBuilder.SelectOrdered)limitable;
	}

	SelectBuilder.SelectOrdered applyCriteria(@Nullable CriteriaDefinition criteria,
		SelectBuilder.SelectWhere whereBuilder, MapSqlParameterSource parameterSource, Table table) {

		return criteria == null || criteria.isEmpty() // Check for null and empty criteria
			? whereBuilder //
			: whereBuilder.where(queryMapper.getMappedObject(parameterSource, criteria, table, entity));
	}

	private Expression getColumnExpression(
		PersistentPropertyPathExtension extPath, Column column) {
		SqlFunction sqlFunction = extPath.getRequiredPersistentPropertyPath().getLeafProperty()
			.findAnnotation(SqlFunction.class);
		if (sqlFunction == null) {
			return column;
		}

		List<Expression> functionExpressions = new ArrayList<>();
		for (String expression : sqlFunction.expressions()) {
			if (SqlFunction.COLUMN_NAME.equals(expression)) {
				functionExpressions.add(column);
			} else {
				functionExpressions.add(Expressions.just(expression));
			}
		}

		SimpleFunction function = SimpleFunction.create(
			sqlFunction.functionName(), functionExpressions);
		if (column instanceof Aliased) {
			function = function.as(((Aliased)column).getAlias());
		}
		return function;
	}

	/**
	 * Value object representing a {@code JOIN} association.
	 */
	static class Join {
		Table joinTable;
		Column joinColumn;
		Column parentId;

		Join(Table joinTable, Column joinColumn, Column parentId) {
			this.joinTable = joinTable;
			this.joinColumn = joinColumn;
			this.parentId = parentId;
		}

		Table getJoinTable() {
			return this.joinTable;
		}

		Column getJoinColumn() {
			return this.joinColumn;
		}

		Column getParentId() {
			return this.parentId;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			Join join = (Join)obj;
			return joinTable.equals(join.joinTable)
				&& joinColumn.equals(join.joinColumn)
				&& parentId.equals(join.parentId);
		}

		@Override
		public String toString() {
			return "Join{" + //
				"joinTable=" + joinTable + //
				", joinColumn=" + joinColumn + //
				", parentId=" + parentId + //
				'}';
		}
	}

	/**
	 * Value object encapsulating column name caches.
	 *
	 * @author Mark Paluch
	 * @author Jens Schauder
	 */
	static class Columns {
		private final MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty>
			mappingContext;
		private final JdbcConverter converter;

		private final List<SqlIdentifier> columnNames = new ArrayList<>();
		private final List<SqlIdentifier> idColumnNames = new ArrayList<>();
		private final List<SqlIdentifier> nonIdColumnNames = new ArrayList<>();
		private final Set<SqlIdentifier> readOnlyColumnNames = new HashSet<>();
		private final Set<SqlIdentifier> insertOnlyColumnNames = new HashSet<>();
		private final Set<SqlIdentifier> insertableColumns;
		private final Set<SqlIdentifier> updatableColumns;

		Columns(RelationalPersistentEntity<?> entity,
			MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext,
			JdbcConverter converter) {

			this.mappingContext = mappingContext;
			this.converter = converter;

			populateColumnNameCache(entity, "");

			Set<SqlIdentifier> insertable = new LinkedHashSet<>(nonIdColumnNames);
			insertable.removeAll(readOnlyColumnNames);

			this.insertableColumns = Collections.unmodifiableSet(insertable);

			Set<SqlIdentifier> updatable = new LinkedHashSet<>(columnNames);

			updatable.removeAll(idColumnNames);
			updatable.removeAll(readOnlyColumnNames);
			updatable.removeAll(insertOnlyColumnNames);

			this.updatableColumns = Collections.unmodifiableSet(updatable);
		}

		private void populateColumnNameCache(
			RelationalPersistentEntity<?> entity, String prefix) {

			entity.doWithAll(property -> {

				// the referencing column of referenced entity is expected to be on the other side
				// of the relation
				if (!property.isEntity()) {
					initSimpleColumnName(property, prefix);
				} else if (property.isEmbedded()) {
					initEmbeddedColumnNames(property, prefix);
				}
			});
		}

		private void initSimpleColumnName(RelationalPersistentProperty property, String prefix) {

			SqlIdentifier columnName = property.getColumnName().transform(prefix::concat);

			columnNames.add(columnName);

			if (!property.getOwner().isIdProperty(property)) {
				nonIdColumnNames.add(columnName);
			} else {
				idColumnNames.add(columnName);
			}

			if (!property.isWritable()) {
				readOnlyColumnNames.add(columnName);
			}

			if (property.isInsertOnly()) {
				insertOnlyColumnNames.add(columnName);
			}
		}

		private void initEmbeddedColumnNames(RelationalPersistentProperty property, String prefix) {

			String embeddedPrefix = property.getEmbeddedPrefix();

			RelationalPersistentEntity<?> embeddedEntity = mappingContext
				.getRequiredPersistentEntity(converter.getColumnType(property));

			populateColumnNameCache(embeddedEntity, prefix + embeddedPrefix);
		}

		/**
		 * @return Column names that can be used for {@code INSERT}.
		 */
		Set<SqlIdentifier> getInsertableColumns() {
			return insertableColumns;
		}

		/**
		 * @return Column names that can be used for {@code UPDATE}.
		 */
		Set<SqlIdentifier> getUpdatableColumns() {
			return updatableColumns;
		}
	}

	/**
	 * DIFF: org.springframework.data.relational.core.sql.DefaultSelect
	 * Only support Select columns for selectColumns
	 */
	class SimpleSelect implements Select {

		private final SelectList selectList;

		SimpleSelect(List<Expression> selectList) {
			try {
				Constructor<?> selectListConstructor =
					ReflectionUtils.findConstructor(SelectList.class, selectList).get();
				selectListConstructor.setAccessible(true);
				this.selectList = (SelectList)selectListConstructor.newInstance(selectList);
			} catch (Exception e) {
				throw new MappingException("Can not created SimpleSelect.");
			}
		}

		@Override
		public void visit(Visitor visitor) {
			visitor.enter(this);
			selectList.visit(visitor);
			visitor.leave(this);
		}

		@Override
		public From getFrom() {
			throw new UnsupportedOperationException("SimpleSelect does not support from clause.");
		}

		@Override
		public List<OrderByField> getOrderBy() {
			return Collections.emptyList();
		}

		@Override
		public OptionalLong getLimit() {
			return OptionalLong.empty();
		}

		@Override
		public OptionalLong getOffset() {
			return OptionalLong.empty();
		}

		@Override
		public boolean isDistinct() {
			return false;
		}

		@Override
		public LockMode getLockMode() {
			return null;
		}
	}
}
