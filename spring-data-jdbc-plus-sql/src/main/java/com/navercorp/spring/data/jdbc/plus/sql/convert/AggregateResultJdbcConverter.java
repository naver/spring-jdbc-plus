/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.sql.convert;

import static java.util.stream.Collectors.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.ApplicationContext;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.jdbc.core.convert.Identifier;
import org.springframework.data.jdbc.core.convert.JdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.mapping.InstanceCreatorMetadata;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.model.CachingValueExpressionEvaluatorFactory;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.mapping.model.SpELContext;
import org.springframework.data.mapping.model.ValueExpressionEvaluator;
import org.springframework.data.mapping.model.ValueExpressionParameterValueProvider;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.util.Streamable;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.navercorp.spring.data.jdbc.plus.support.convert.PropertyPathUtils;

/**
 * The type Aggregate result jdbc converter.
 *
 * @author Myeonghyeon Lee
 */
public class AggregateResultJdbcConverter extends MappingJdbcConverter {
	private SpELContext spElContext;

	private final ExpressionParser expressionParser = new SpelExpressionParser();

	private final SpelAwareProxyProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory(
		expressionParser);

	private final CachingValueExpressionEvaluatorFactory valueExpressionEvaluatorFactory = new CachingValueExpressionEvaluatorFactory(
		expressionParser, this, o -> spElContext.getEvaluationContext(o));

	/**
	 * Instantiates a new Aggregate result jdbc converter.
	 *
	 * @param context          the context
	 * @param relationResolver the relation resolver
	 */
	public AggregateResultJdbcConverter(
		RelationalMappingContext context,
		RelationResolver relationResolver
	) {
		super(context, relationResolver);
		this.spElContext = new SpELContext(ResultMapPropertyAccessor.INSTANCE);
	}

	/**
	 * Instantiates a new Aggregate result jdbc converter.
	 *
	 * @param context              the context
	 * @param relationResolver     the relation resolver
	 * @param conversions          the conversions
	 * @param typeFactory          the type factory
	 */
	public AggregateResultJdbcConverter(
		RelationalMappingContext context,
		RelationResolver relationResolver,
		CustomConversions conversions,
		JdbcTypeFactory typeFactory
	) {
		super(context, relationResolver, conversions, typeFactory);
		this.spElContext = new SpELContext(ResultMapPropertyAccessor.INSTANCE);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		super.setApplicationContext(applicationContext);
		this.spElContext = new SpELContext(this.spElContext, applicationContext);
	}

	/**
	 * Map aggregate list.
	 *
	 * @param <T>       the type parameter
	 * @param entity    the entity
	 * @param resultSet the result set
	 * @return the list
	 */
	public final <T> List<T> mapAggregate(RelationalPersistentEntity<T> entity, ResultSet resultSet) {
		try {
			ResultSetHolder resultSetHolder = new ResultSetHolder(resultSet);
			EntityPathRelations entityPathRelations = this.getEntityPathRelations(entity);
			List<Map<String, Object>> aggregateMapList = this.extractData(resultSetHolder, entityPathRelations);

			List<T> result = new ArrayList<>();
			for (Map<String, Object> aggregateMap : aggregateMapList) {
				T aggregate = this.mapAggregate(entity, aggregateMap);
				result.add(aggregate);
			}

			return result;
		} catch (Exception e) {
			throw new MappingException("Result aggregate failure. entity: " + entity.getType(), e);
		}
	}

	/**
	 * Single Table ResultSet to Map
	 *
	 * @param entity    the entity
	 * @param resultSet the result set
	 * @return the map
	 */
	protected Map<String, Object> mapSingleTableRow(RelationalPersistentEntity<?> entity, ResultSet resultSet) {
		return new SingleTableMapReadingContext(
			getMappingContext().getAggregatePath(entity),
			new ResultSetAccessor(resultSet),
			Identifier.empty(),
			null
		).mapRow();
	}

	/**
	 * Single Table ResultSet to Map
	 *
	 * @param path       the path
	 * @param resultSet  the result set
	 * @param identifier the identifier
	 * @return the map
	 */
	protected Map<String, Object> mapSingleTableRow(
		AggregatePath path,
		ResultSet resultSet,
		Identifier identifier
	) {
		return new SingleTableMapReadingContext(
			path.getLeafEntity(),
			new ResultSetAccessor(resultSet),
			path.getParentPath(),
			path,
			identifier,
			null
		).mapRow();
	}

	/**
	 * Single Table ResultSet to Key, Map(Value)
	 *
	 * @param path       the path
	 * @param resultSet  the result set
	 * @param identifier the identifier
	 * @param key        the key
	 * @return the map . entry
	 */
	protected Map.Entry<Object, Map<String, Object>> mapSingleTableMapRow(
		AggregatePath path,
		ResultSet resultSet,
		Identifier identifier,
		Object key
	) {
		Map<String, Object> mapValue = new SingleTableMapReadingContext(
			path.getLeafEntity(),
			new ResultSetAccessor(resultSet),
			path.getParentPath(),
			path,
			identifier,
			key
		).mapRow();

		return new HashMap.SimpleEntry<>(key, mapValue);
	}

	/**
	 * Map aggregate t.
	 *
	 * @param <T>          the type parameter
	 * @param entity       the entity
	 * @param aggregateMap the aggregate map
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	protected <T> T mapAggregate(
		RelationalPersistentEntity<T> entity,
		Map<String, Object> aggregateMap
	) {
		return (T)new MapReadingContext<>(
			getMappingContext().getAggregatePath(entity),
			aggregateMap
		).mapRow();
	}

	private List<Map<String, Object>> extractData(
		ResultSetHolder resultSetHolder,
		EntityPathRelations entityPathRelations
	) throws SQLException {

		AggregatePath rootPath = entityPathRelations.getRootPath();
		RelationalPersistentEntity<?> persistentEntity = rootPath.getLeafEntity();

		Map<Object, ExtractedRow> extractedRows = new LinkedHashMap<>();

		while (!resultSetHolder.isDone() && resultSetHolder.next()) {
			Map<String, Object> entityMap = this.mapSingleTableRow(
				persistentEntity, resultSetHolder.getResultSet());

			Object rootId = this.getRootId(resultSetHolder, persistentEntity);
			ExtractedRow rootRow = extractedRows.get(rootId);
			if (rootRow == null) {
				rootRow = new ExtractedRow(
					null, persistentEntity, entityMap, rootId, null, new LinkedMultiValueMap<>());
				extractedRows.put(rootId, rootRow);
			}
			this.appendExtractRelationRows(
				resultSetHolder, rootRow, entityPathRelations.getRelations());
		}

		List<Map<String, Object>> result = new ArrayList<>(extractedRows.size());
		for (Map.Entry<Object, ExtractedRow> row : extractedRows.entrySet()) {
			ExtractedRow extractedRow = row.getValue();
			MultiValueMap<AggregatePath, RelationValue> relations =
				this.accumulateRelations(extractedRow.getRelations());
			this.setEntityRelations(
				extractedRow.getRoot(), extractedRow.getRootEntity(), relations);
			result.add(extractedRow.getRoot());
		}

		return result;
	}

	private void appendExtractRelationRows(
		ResultSetHolder resultSetHolder,
		ExtractedRow rootRow,
		Map<AggregatePath, EntityPathRelations> relationEntityPaths
	) throws SQLException {

		MultiValueMap<AggregatePath, ExtractedRow> relationEntities = rootRow.getRelations();

		for (Map.Entry<AggregatePath, EntityPathRelations> relationEntityPath
			: relationEntityPaths.entrySet()) {

			AggregatePath relationPath = relationEntityPath.getKey();
			List<ExtractedRow> relationRows = relationEntities.get(relationPath);

			// extract relation entity Id
			String idColumnAlias = this.getIdColumnAlias(relationEntityPath.getKey());
			Object relationEntityId = resultSetHolder.getResultSet().getObject(idColumnAlias);

			if (relationEntityId == null) {
				continue;
			}

			Identifier identifier = this.getRelationEntityIdentifier(
				relationPath, rootRow.getRootEntity(), rootRow.getRoot());

			if (CollectionUtils.isEmpty(relationRows)) {
				// First relation extract
				ExtractedRow extractedRow = this.extractRelationRow(
					resultSetHolder,
					rootRow.getRootId(),
					relationPath,
					identifier,
					relationEntityId,
					relationEntityPath.getValue().getRelations());

				List<ExtractedRow> newRelations = new ArrayList<>();
				newRelations.add(extractedRow);

				relationEntities.put(relationPath, newRelations);
				continue;
			}

			ExtractedRow existRow = null;
			for (ExtractedRow relationRow : relationRows) {
				if (relationRow.getRootId().equals(relationEntityId)) {
					// Next row relation is same entity
					existRow = relationRow;
					break;
				}
			}

			if (existRow != null) {
				// extract relation rows
				this.appendExtractRelationRows(
					resultSetHolder,
					existRow,
					relationEntityPath.getValue().getRelations());
			} else {
				// extract new relation rows
				ExtractedRow relationExtractedRows = this.extractRelationRow(
					resultSetHolder,
					rootRow.getRootId(),
					relationPath,
					identifier,
					relationEntityId,
					relationEntityPath.getValue().getRelations());
				relationEntities.add(relationPath, relationExtractedRows);
			}
		}
	}

	private ExtractedRow extractRelationRow(
		ResultSetHolder resultSetHolder,
		Object rootId,
		AggregatePath relationPath,
		Identifier identifier,
		Object relationEntityId,
		Map<AggregatePath, EntityPathRelations> nestedRelations
	) throws SQLException {

		Map<String, Object> relationValue;
		Object key = null;
		if (relationPath.isMap()) {
			String keyColumn = this.getQualifierColumnAlias(relationPath);
			key = resultSetHolder.getResultSet().getObject(keyColumn);
			Map.Entry<Object, Map<String, Object>> relationMapEntry = this.mapSingleTableMapRow(
				relationPath, resultSetHolder.getResultSet(), identifier, key);
			relationValue = relationMapEntry.getValue();
		} else {
			relationValue = this.mapSingleTableRow(
				relationPath, resultSetHolder.getResultSet(), identifier);
		}

		ExtractedRow extractedRow = new ExtractedRow(
			rootId,
			relationPath.getLeafEntity(),
			relationValue,
			relationEntityId,
			key,
			new LinkedMultiValueMap<>());
		this.appendExtractRelationRows(resultSetHolder, extractedRow, nestedRelations);
		return extractedRow;
	}

	private EntityPathRelations getEntityPathRelations(RelationalPersistentEntity<?> entity) {
		AggregatePath rootPath = this.getMappingContext().getAggregatePath(entity);
		return this.getEntityPathRelations(rootPath);
	}

	private EntityPathRelations getEntityPathRelations(AggregatePath entityPath) {
		Map<AggregatePath, EntityPathRelations> relations = new HashMap<>();

		for (RelationalPersistentProperty property : entityPath.getLeafEntity()) {
			if (property.isEmbedded()) {
				continue;
			}

			if (property.isEntity() || (property.isCollectionLike() && property.isEntity()) || property.isMap()) {
				PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath =
					entityPath.append(property).getRequiredPersistentPropertyPath();
				AggregatePath relationPath = this.getMappingContext().getAggregatePath(propertyPath);
				EntityPathRelations entityPathRelations = this.getEntityPathRelations(relationPath);
				relations.put(relationPath, entityPathRelations);
			}
		}

		return new EntityPathRelations(entityPath, relations);
	}

	private MultiValueMap<AggregatePath, RelationValue> accumulateRelations(
		MultiValueMap<AggregatePath, ExtractedRow> extractedRows
	) {
		MultiValueMap<AggregatePath, RelationValue> relations = new LinkedMultiValueMap<>();
		for (Map.Entry<AggregatePath, List<ExtractedRow>> extractedRow : extractedRows.entrySet()) {

			AggregatePath path = extractedRow.getKey();
			List<ExtractedRow> rowValues = extractedRow.getValue();
			for (ExtractedRow rowValue : rowValues) {
				relations.add(path, RelationValue.from(rowValue));
				relations.addAll(this.accumulateRelations(rowValue.getRelations()));
			}
		}
		return relations;
	}

	private void setEntityRelations(
		Map<String, Object> rootEntity,
		RelationalPersistentEntity<?> persistentEntity,
		MultiValueMap<AggregatePath, RelationValue> relationValues
	) {
		for (Map.Entry<AggregatePath, List<RelationValue>> relations : relationValues.entrySet()) {

			AggregatePath propertyPath = relations.getKey();
			RelationalPersistentProperty property =
				propertyPath.getRequiredPersistentPropertyPath().getLeafProperty();

			// Set relations for root entity
			if (persistentEntity.getType() == property.getOwner().getType()) {
				rootEntity.put(property.getName(), this.determineRelationValue(property, relations.getValue()));
			} else {
				Map<Object, RelationValue> parentValues = relationValues.getOrDefault(
						propertyPath.getParentPath(),
						new ArrayList<>()
					).stream()
					.collect(toMap(RelationValue::getValueId, it -> it));

				MultiValueMap<Object, RelationValue> parentKeyChildren = new LinkedMultiValueMap<>();
				for (RelationValue value : relations.getValue()) {
					parentKeyChildren.add(value.getParentId(), value);
				}

				for (Map.Entry<Object, List<RelationValue>> parentKeyChild : parentKeyChildren.entrySet()) {

					Map<String, Object> parentValue = parentValues.get(parentKeyChild.getKey()).getValue();
					List<RelationValue> relationValue = parentKeyChild.getValue();
					parentValue.put(property.getName(), this.determineRelationValue(property, relationValue));
				}
			}
		}
	}

	private Object getRootId(
		ResultSetHolder resultSet,
		RelationalPersistentEntity<?> entity
	) throws SQLException {
		return resultSet.getResultSet().getObject(entity.getIdColumn().getReference());
	}

	private Identifier getRelationEntityIdentifier(
		AggregatePath relationPath,
		RelationalPersistentEntity<?> entity,
		Map<String, Object> entityMap
	) {
		Object id = entityMap.get(entity.getRequiredIdProperty().getName());
		return Identifier.of(relationPath.getTableInfo().reverseColumnInfo().name(), id, Object.class);
	}

	protected String getIdColumnAlias(AggregatePath relationPath) {
		return PropertyPathUtils.getColumnAlias(
			relationPath.append(relationPath.getLeafEntity().getRequiredIdProperty())
		).getReference();
	}

	protected String getQualifierColumnAlias(AggregatePath relationPath) {
		return PropertyPathUtils.getTableAlias(relationPath).getReference()
			+ "_" + relationPath.getTableInfo().qualifierColumnInfo().name().getReference();
	}

	private Object determineRelationValue(
		RelationalPersistentProperty property,
		List<RelationValue> relationValue
	) {
		if (property.isMap()) {
			return relationValue.stream()
				.distinct()
				.collect(toMap(RelationValue::getKeyValue, RelationValue::getValue));
		}

		List<Map<String, Object>> relationMapValues = relationValue.stream()
			.map(RelationValue::getValue)
			.distinct()
			.collect(toList());

		if (!property.isCollectionLike() && (property.isEntity() || property.isEmbedded())) {
			if (relationMapValues.size() > 1) {
				throw new MappingException(String.format(
					"Could not mapping path %s from value %s. "
						+ "property is entity but multiple value.",
					property.getOwner().getType() + "#" + property.getName(), relationMapValues));
			}

			if (relationMapValues.isEmpty()) {
				return null;
			} else {
				return relationMapValues.get(0);
			}
		}

		return relationMapValues;
	}

	private boolean isSimpleProperty(RelationalPersistentProperty property) {
		return !property.isCollectionLike()
			&& !property.isEntity()
			&& !property.isMap()
			&& !property.isEmbedded();
	}

	/**
	 * The interface Map parameter value provider.
	 *
	 * @param <P> the type parameter
	 */
	protected interface MapParameterValueProvider<P extends PersistentProperty<P>> {
		/**
		 * Gets parameter value.
		 *
		 * @param parameter the parameter
		 * @return the parameter value
		 */
		@Nullable
		Object getParameterValue(Parameter<?, P> parameter);
	}

	private static class ResultSetHolder {
		private ResultSet resultSet;
		private int currentRowNum;
		private boolean done;

		/**
		 * Instantiates a new Result set holder.
		 *
		 * @param resultSet the result set
		 */
		public ResultSetHolder(ResultSet resultSet) {
			this.resultSet = resultSet;
			this.currentRowNum = -1;
			this.done = false;
		}

		/**
		 * Next boolean.
		 *
		 * @return the boolean
		 * @throws SQLException the sql exception
		 */
		public boolean next() throws SQLException {
			boolean next = this.resultSet.next();
			this.currentRowNum++;
			if (!next) {
				this.done = true;
			}
			return next;
		}

		/**
		 * Is done boolean.
		 *
		 * @return the boolean
		 */
		public boolean isDone() {
			return this.done;
		}

		/**
		 * Gets result set.
		 *
		 * @return the result set
		 */
		public ResultSet getResultSet() {
			return this.resultSet;
		}

		/**
		 * Gets current row num.
		 *
		 * @return the current row num
		 */
		public int getCurrentRowNum() {
			return this.currentRowNum;
		}
	}

	private static class EntityPathRelations {
		private final AggregatePath rootPath;
		private final Map<AggregatePath, EntityPathRelations> relations;

		/**
		 * Instantiates a new Entity path relations.
		 *
		 * @param rootPath  the root path
		 * @param relations the relations
		 */
		EntityPathRelations(
			AggregatePath rootPath,
			Map<AggregatePath, EntityPathRelations> relations) {

			this.rootPath = rootPath;
			this.relations = relations;
		}

		/**
		 * Gets root path.
		 *
		 * @return the root path
		 */
		public AggregatePath getRootPath() {
			return this.rootPath;
		}

		/**
		 * Gets relations.
		 *
		 * @return the relations
		 */
		public Map<AggregatePath, EntityPathRelations> getRelations() {
			return this.relations;
		}
	}

	private static class ExtractedRow {
		private final Object parentId;
		private final RelationalPersistentEntity<?> rootEntity;
		private final Map<String, Object> root;
		private final Object rootId;
		private final Object keyValue;
		private final MultiValueMap<AggregatePath, ExtractedRow> relations;

		/**
		 * Instantiates a new Extracted row.
		 *
		 * @param parentId   the parent id
		 * @param rootEntity the root entity
		 * @param root       the root
		 * @param rootId     the root id
		 * @param keyValue   the key value
		 * @param relations  the relations
		 */
		ExtractedRow(
			Object parentId,
			RelationalPersistentEntity<?> rootEntity,
			Map<String, Object> root,
			Object rootId,
			Object keyValue,
			MultiValueMap<AggregatePath, ExtractedRow> relations
		) {
			this.parentId = parentId;
			this.rootEntity = rootEntity;
			this.root = root;
			this.rootId = rootId;
			this.keyValue = keyValue;
			this.relations = relations;
		}

		/**
		 * Gets parent id.
		 *
		 * @return the parent id
		 */
		public Object getParentId() {
			return this.parentId;
		}

		/**
		 * Gets root entity.
		 *
		 * @return the root entity
		 */
		public RelationalPersistentEntity<?> getRootEntity() {
			return this.rootEntity;
		}

		/**
		 * Gets root.
		 *
		 * @return the root
		 */
		public Map<String, Object> getRoot() {
			return this.root;
		}

		/**
		 * Gets root id.
		 *
		 * @return the root id
		 */
		public Object getRootId() {
			return this.rootId;
		}

		/**
		 * Gets relations.
		 *
		 * @return the relations
		 */
		public MultiValueMap<AggregatePath, ExtractedRow> getRelations() {
			return this.relations;
		}

		/**
		 * Gets key value.
		 *
		 * @return the key value
		 */
		public Object getKeyValue() {
			return this.keyValue;
		}
	}

	private static class RelationValue {
		private final Object parentId;
		private final Object valueId;
		private final Object keyValue;
		private final Map<String, Object> value;

		/**
		 * Instantiates a new Relation value.
		 *
		 * @param parentId the parent id
		 * @param valueId  the value id
		 * @param keyValue the key value
		 * @param value    the value
		 */
		public RelationValue(
			Object parentId,
			Object valueId,
			Object keyValue,
			Map<String, Object> value
		) {
			this.parentId = parentId;
			this.valueId = valueId;
			this.keyValue = keyValue;
			this.value = value;
		}

		/**
		 * Gets parent id.
		 *
		 * @return the parent id
		 */
		public Object getParentId() {
			return this.parentId;
		}

		/**
		 * Gets value id.
		 *
		 * @return the value id
		 */
		public Object getValueId() {
			return this.valueId;
		}

		/**
		 * Gets key value.
		 *
		 * @return the key value
		 */
		public Object getKeyValue() {
			return this.keyValue;
		}

		/**
		 * Gets value.
		 *
		 * @return the value
		 */
		public Map<String, Object> getValue() {
			return this.value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			RelationValue that = (RelationValue)obj;
			return Objects.equals(parentId, that.parentId)
				&& Objects.equals(valueId, that.valueId)
				&& Objects.equals(keyValue, that.keyValue)
				&& Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(parentId, valueId, keyValue, value);
		}

		private static RelationValue from(ExtractedRow rowValue) {
			return new RelationValue(
				rowValue.getParentId(),
				rowValue.getRootId(),
				rowValue.getKeyValue(),
				rowValue.getRoot()
			);
		}
	}

	private class SingleTableMapReadingContext {

		private final RelationalPersistentEntity<?> entity;

		private final AggregatePath rootPath;
		private final AggregatePath path;
		private final Identifier identifier;
		private final Object key;

		private final JdbcPropertyValueProvider propertyValueProvider;
		private final JdbcBackReferencePropertyValueProvider backReferencePropertyValueProvider;
		private final ResultSetAccessor accessor;

		private SingleTableMapReadingContext(
			AggregatePath rootPath,
			ResultSetAccessor accessor,
			Identifier identifier,
			Object key
		) {
			RelationalPersistentEntity<?> entity = rootPath.getLeafEntity();

			Assert.notNull(entity, "The rootPath must point to an entity.");

			this.entity = entity;
			this.rootPath = rootPath;
			this.path = getMappingContext().getAggregatePath(this.entity);
			this.identifier = identifier;
			this.key = key;
			this.propertyValueProvider = new JdbcPropertyValueProvider(path, accessor);
			this.backReferencePropertyValueProvider = new JdbcBackReferencePropertyValueProvider(path, accessor);
			this.accessor = accessor;
		}

		private SingleTableMapReadingContext(
			RelationalPersistentEntity<?> entity,
			ResultSetAccessor accessor,
			AggregatePath rootPath,
			AggregatePath path,
			Identifier identifier,
			Object key
		) {
			this.entity = entity;
			this.rootPath = rootPath;
			this.path = path;
			this.identifier = identifier;
			this.key = key;
			this.propertyValueProvider = new JdbcPropertyValueProvider(path, accessor);
			this.backReferencePropertyValueProvider = new JdbcBackReferencePropertyValueProvider(path, accessor);
			this.accessor = accessor;
		}

		private SingleTableMapReadingContext(
			RelationalPersistentEntity<?> entity,
			AggregatePath rootPath,
			AggregatePath path,
			Identifier identifier,
			Object key,
			JdbcPropertyValueProvider propertyValueProvider,
			JdbcBackReferencePropertyValueProvider backReferencePropertyValueProvider,
			ResultSetAccessor accessor
		) {
			this.entity = entity;
			this.rootPath = rootPath;
			this.path = path;
			this.identifier = identifier;
			this.key = key;

			this.propertyValueProvider = propertyValueProvider;
			this.backReferencePropertyValueProvider = backReferencePropertyValueProvider;
			this.accessor = accessor;
		}

		private SingleTableMapReadingContext extendBy(RelationalPersistentProperty property) {
			return new SingleTableMapReadingContext(
				getMappingContext().getRequiredPersistentEntity(property.getActualType()),
				/* rootPath.extendBy(property)*/ rootPath, path.append(property), identifier, key,
				propertyValueProvider.extendBy(property),
				backReferencePropertyValueProvider.extendBy(property), accessor);
		}

		/**
		 * Map row map.
		 *
		 * @return the map
		 */
		Map<String, Object> mapRow() {

			RelationalPersistentProperty idProperty = entity.getIdProperty();

			Object idValue = idProperty == null ? null : readFrom(idProperty);

			return createInstanceInternal(idValue);
		}

		private void populateProperties(Map<String, Object> map, @Nullable Object idValue) {
			entity.doWithAll(property -> {
				// skip absent simple properties
				if (isSimpleProperty(property)) {

					if (!propertyValueProvider.hasProperty(property)) {
						return;
					}
				}

				Object value = readOrLoadProperty(idValue, property);
				map.put(property.getName(), value);
			});
		}

		@Nullable
		private Object readOrLoadProperty(
			@Nullable Object id, RelationalPersistentProperty property) {

			if (property.isCollectionLike() && property.isEntity()) {
				return new ArrayList<>();
			} else if (property.isMap()) {
				return new HashMap<>();
			} else if (property.isEmbedded()) {
				return readEmbeddedEntityFrom(id, property);
			} else {
				return readFrom(property);
			}
		}

		@Nullable
		private Object readFrom(RelationalPersistentProperty property) {

			if (property.isEntity()) {
				return readEntityFrom(property);
			}

			Object value = propertyValueProvider.getPropertyValue(property);
			return value != null ? readValue(value, property.getTypeInformation()) : null;
		}

		@Nullable
		private Object readEmbeddedEntityFrom(
			@Nullable Object idValue, RelationalPersistentProperty property) {

			SingleTableMapReadingContext newContext = extendBy(property);

			if (shouldCreateEmptyEmbeddedInstance(property) || newContext.hasInstanceValues(idValue)) {
				return newContext.createInstanceInternal(idValue);
			}

			return null;
		}

		private boolean shouldCreateEmptyEmbeddedInstance(
			RelationalPersistentProperty property) {

			return property.shouldCreateEmptyEmbedded();
		}

		private boolean hasInstanceValues(@Nullable Object idValue) {

			RelationalPersistentEntity<?> persistentEntity = path.getRequiredLeafEntity();

			for (RelationalPersistentProperty embeddedProperty : persistentEntity) {

				// if the embedded contains Lists, Sets or Maps we consider it non-empty
				if (embeddedProperty.isQualified() || embeddedProperty.isAssociation()) {
					return true;
				}

				Object value = readOrLoadProperty(idValue, embeddedProperty);
				if (value != null) {
					return true;
				}
			}

			return false;
		}

		@Nullable
		@SuppressWarnings("unchecked")
		private Object readEntityFrom(RelationalPersistentProperty property) {

			SingleTableMapReadingContext newContext = extendBy(property);
			RelationalPersistentEntity<?> entity = getMappingContext()
				.getRequiredPersistentEntity(property.getActualType());
			RelationalPersistentProperty idProperty = entity.getIdProperty();

			Object idValue;

			if (idProperty != null) {
				idValue = newContext.readFrom(idProperty);
			} else {
				idValue = backReferencePropertyValueProvider.getPropertyValue(property);
			}

			if (idValue == null) {
				return null;
			}

			return newContext.createInstanceInternal(idValue);
		}

		private Map<String, Object> createInstanceInternal(@Nullable Object idValue) {
			Map<String, Object> map = new HashMap<>();
			populateProperties(map, idValue);
			return map;
		}

	}

	private class MapReadingContext<T> {

		private final RelationalPersistentEntity<T> entity;

		private final Map<String, Object> entityMap;
		private final AggregatePath path;

		@SuppressWarnings("unchecked")
		private MapReadingContext(
			AggregatePath rootPath,
			@Nullable Map<String, Object> entityMap
		) {
			RelationalPersistentEntity<T> entity = (RelationalPersistentEntity<T>)rootPath.getLeafEntity();

			Assert.notNull(entity, "The rootPath must point to an entity.");

			this.entity = entity;
			this.entityMap = entityMap;
			this.path = getMappingContext().getAggregatePath(this.entity);
		}

		private MapReadingContext(
			RelationalPersistentEntity<T> entity,
			@Nullable Map<String, Object> entityMap,
			AggregatePath path
		) {
			this.entity = entity;
			this.entityMap = entityMap;
			this.path = path;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private <S> MapReadingContext<S> extendEntityBy(
			RelationalPersistentProperty property,
			@Nullable Map<String, Object> entityMap
		) {
			return new MapReadingContext(
				getMappingContext().getRequiredPersistentEntity(property.getActualType()),
				entityMap,
				path.append(property)
			);
		}

		/**
		 * Map row t.
		 *
		 * @return the t
		 */
		T mapRow() {

			RelationalPersistentProperty idProperty = entity.getIdProperty();

			Object idValue = idProperty == null ? null : readFrom(idProperty);

			return createInstanceInternal(idValue);
		}

		private T populateProperties(T instance, @Nullable Object idValue) {

			PersistentPropertyAccessor<T> propertyAccessor = getPropertyAccessor(entity, instance);

			InstanceCreatorMetadata<RelationalPersistentProperty> creatorMetadata =
				entity.getInstanceCreatorMetadata();

			entity.doWithAll(property -> {

				if (creatorMetadata != null
					&& creatorMetadata.isCreatorParameter(property)) {
					return;
				}

				// skip absent simple properties
				if (isSimpleProperty(property)) {
					if (this.entityMap == null || !this.entityMap.containsKey(property.getName())) {
						return;
					}
				}

				Object value = readOrLoadProperty(idValue, property);
				propertyAccessor.setProperty(property, value);
			});

			return propertyAccessor.getBean();
		}

		@Nullable
		private Object readOrLoadProperty(
			@Nullable Object id,
			RelationalPersistentProperty property
		) {
			if (property.isMap()) {
				return this.readMapFrom(property);
			} else if (property.isCollectionLike() && property.isEntity()) {
				return this.readCollectionFrom(property);
			} else if (property.isEmbedded()) {
				return readEmbeddedEntityFrom(id, property);
			} else {
				return readFrom(property);
			}
		}

		@Nullable
		private Object readFrom(RelationalPersistentProperty property) {

			if (property.isEntity()) {
				return readEntityFrom(property);
			}

			Object value = getObjectFromResultSet(property.getName());
			return value != null ? readValue(value, property.getTypeInformation()) : null;
		}

		@SuppressWarnings("unchecked")
		@Nullable
		private Object readEmbeddedEntityFrom(
			@Nullable Object idValue,
			RelationalPersistentProperty property
		) {
			if (this.entityMap == null) {
				return null;
			}

			Object value = this.entityMap.get(property.getName());
			MapReadingContext<?> newContext = extendEntityBy(property, (Map<String, Object>)value);

			if (shouldCreateEmptyEmbeddedInstance(property) || newContext.hasInstanceValues(idValue)) {
				return newContext.createInstanceInternal(idValue);
			}

			return null;
		}

		private boolean shouldCreateEmptyEmbeddedInstance(
			RelationalPersistentProperty property
		) {
			return property.shouldCreateEmptyEmbedded();
		}

		private boolean hasInstanceValues(@Nullable Object idValue) {

			RelationalPersistentEntity<?> persistentEntity = path.getLeafEntity();

			for (RelationalPersistentProperty embeddedProperty : persistentEntity) {

				// if the embedded contains Lists, Sets or Maps we consider it non-empty
				if (embeddedProperty.isQualified() || embeddedProperty.isAssociation()) {
					return true;
				}

				Object value = readOrLoadProperty(idValue, embeddedProperty);
				if (value != null) {
					return true;
				}
			}

			return false;
		}

		@Nullable
		@SuppressWarnings("unchecked")
		private Object readEntityFrom(RelationalPersistentProperty property) {
			if (this.entityMap == null) {
				return null;
			}

			Map<String, Object> value = (Map<String, Object>)this.entityMap.get(property.getName());
			return this.readEntityFrom(property, value);
		}

		@Nullable
		@SuppressWarnings("unchecked")
		private Object readEntityFrom(
			RelationalPersistentProperty property,
			@Nullable Map<String, Object> value
		) {
			if (value == null) {
				return null;
			}

			MapReadingContext<?> newContext = extendEntityBy(property, value);
			RelationalPersistentEntity<?> entity = getMappingContext()
				.getRequiredPersistentEntity(property.getActualType());
			RelationalPersistentProperty idProperty = entity.getIdProperty();

			Object idValue;

			if (idProperty != null) {
				idValue = newContext.readFrom(idProperty);
			} else {
				idValue = newContext.getObjectFromResultSet(property.getName());
			}

			if (idValue == null) {
				return null;
			}

			return newContext.createInstanceInternal(idValue);
		}

		private Map<Object, Object> readMapFrom(RelationalPersistentProperty property) {
			if (this.entityMap == null) {
				return new HashMap<>();
			}

			@SuppressWarnings("unchecked")
			Map<Object, Map<String, Object>> mapValues = (Map<Object, Map<String, Object>>)
				this.entityMap.get(property.getName());
			if (mapValues == null) {
				return new HashMap<>();
			}

			return mapValues.entrySet().stream()
				.map(entry -> new HashMap.SimpleEntry<>(
					entry.getKey(), this.readEntityFrom(property, entry.getValue())))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		}

		private List<Object> readCollectionFrom(RelationalPersistentProperty property) {
			if (this.entityMap == null) {
				return new ArrayList<>();
			}

			@SuppressWarnings("unchecked")
			Iterable<Map<String, Object>> collectionValues = (Iterable<Map<String, Object>>)
				this.entityMap.get(property.getName());
			if (collectionValues == null) {
				return new ArrayList<>();
			}

			return Streamable.of(collectionValues).stream()
				.map(value -> this.readEntityFrom(property, value))
				.collect(toList());
		}

		@Nullable
		private Object getObjectFromResultSet(String propertyName) {
			if (this.entityMap == null) {
				return null;
			}

			return this.entityMap.get(propertyName);
		}

		private T createInstanceInternal(@Nullable Object idValue) {

			InstanceCreatorMetadata<RelationalPersistentProperty> creatorMetadata =
				entity.getInstanceCreatorMetadata();
			ParameterValueProvider<RelationalPersistentProperty> provider;

			if (creatorMetadata != null && creatorMetadata.hasParameters()) {
				ValueExpressionEvaluator expressionEvaluator = valueExpressionEvaluatorFactory.create(this.entityMap);
				provider = new ValueExpressionParameterValueProvider<>(expressionEvaluator, getConversionService(),
					new ResultSetParameterValueProvider(idValue, entity));
			} else {
				provider = NoOpParameterValueProvider.INSTANCE;
			}

			T instance = createInstance(entity, provider::getParameterValue);

			return entity.requiresPropertyPopulation() ? populateProperties(instance, idValue) : instance;
		}

		/**
		 * {@link ParameterValueProvider} that reads a simple property or materializes an object for a
		 * {@link RelationalPersistentProperty}.
		 *
		 * @see #readOrLoadProperty(Object, RelationalPersistentProperty)
		 * @since 2.1
		 */
		private class ResultSetParameterValueProvider implements ParameterValueProvider<RelationalPersistentProperty> {

			private final @Nullable
			Object idValue;
			private final RelationalPersistentEntity<?> entity;

			public ResultSetParameterValueProvider(@Nullable Object idValue, RelationalPersistentEntity<?> entity) {
				this.idValue = idValue;
				this.entity = entity;
			}

			/*
			 * (non-Javadoc)
			 * @see org.springframework.data.mapping.model.ParameterValueProvider#getParameterValue(org.springframework.data.mapping.Parameter)
			 */
			@Override
			@Nullable
			public <T> T getParameterValue(Parameter<T, RelationalPersistentProperty> parameter) {

				String parameterName = parameter.getName();

				Assert.notNull(parameterName,
					"A constructor parameter name must not be null to be used with Spring Data JDBC");

				RelationalPersistentProperty property = entity.getRequiredPersistentProperty(parameterName);
				return (T)readOrLoadProperty(idValue, property);
			}
		}
	}

	enum NoOpParameterValueProvider implements ParameterValueProvider<RelationalPersistentProperty> {

		INSTANCE;

		@Override
		public <T> T getParameterValue(Parameter<T, RelationalPersistentProperty> parameter) {
			return null;
		}
	}
}
