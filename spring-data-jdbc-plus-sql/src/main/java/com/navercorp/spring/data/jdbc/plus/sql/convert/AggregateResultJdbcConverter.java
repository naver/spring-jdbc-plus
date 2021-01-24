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
import org.springframework.data.jdbc.core.convert.BasicJdbcConverter;
import org.springframework.data.jdbc.core.convert.Identifier;
import org.springframework.data.jdbc.core.convert.JdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.DefaultSpELExpressionEvaluator;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.mapping.model.SpELContext;
import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.mapping.model.SpELExpressionParameterValueProvider;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * The type Aggregate result jdbc converter.
 *
 * @author Myeonghyeon Lee
 */
public class AggregateResultJdbcConverter extends BasicJdbcConverter {
	private final IdentifierProcessing identifierProcessing;
	private SpELContext spElContext;

	/**
	 * Instantiates a new Aggregate result jdbc converter.
	 *
	 * @param context          the context
	 * @param relationResolver the relation resolver
	 */
	public AggregateResultJdbcConverter(
		MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> context,
		RelationResolver relationResolver) {

		super(context, relationResolver);
		this.identifierProcessing = IdentifierProcessing.ANSI;
		this.spElContext = new SpELContext(ResultMapPropertyAccessor.INSTANCE);
	}

	/**
	 * Instantiates a new Aggregate result jdbc converter.
	 *
	 * @param context              the context
	 * @param relationResolver     the relation resolver
	 * @param conversions          the conversions
	 * @param typeFactory          the type factory
	 * @param identifierProcessing the identifier processing
	 */
	public AggregateResultJdbcConverter(
		MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> context,
		RelationResolver relationResolver,
		CustomConversions conversions,
		JdbcTypeFactory typeFactory,
		IdentifierProcessing identifierProcessing) {

		super(context, relationResolver, conversions, typeFactory, identifierProcessing);
		this.identifierProcessing = identifierProcessing;
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
			List<Map<String, Object>> aggregateMapList = this.extractData(
				resultSetHolder, entityPathRelations);

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
			new PersistentPropertyPathExtension(getMappingContext(), entity),
			new ResultSetAccessor(resultSet), Identifier.empty(), null).mapRow();
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
		PersistentPropertyPathExtension path, ResultSet resultSet, Identifier identifier) {

		return new SingleTableMapReadingContext(
			path.getLeafEntity(),
			new ResultSetAccessor(resultSet),
			path.getParentPath(),
			path,
			identifier,
			null)
			.mapRow();
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
		PersistentPropertyPathExtension path,
		ResultSet resultSet,
		Identifier identifier,
		Object key) {

		Map<String, Object> mapValue = new SingleTableMapReadingContext(
			path.getLeafEntity(),
			new ResultSetAccessor(resultSet),
			path.getParentPath(),
			path,
			identifier,
			key)
			.mapRow();
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
		RelationalPersistentEntity<T> entity, Map<String, Object> aggregateMap) {

		return (T)new MapReadingContext<>(
			new PersistentPropertyPathExtension(getMappingContext(), entity),
			aggregateMap)
			.mapRow();
	}

	private List<Map<String, Object>> extractData(
		ResultSetHolder resultSetHolder,
		EntityPathRelations entityPathRelations) throws SQLException {

		PersistentPropertyPathExtension rootPath = entityPathRelations.getRootPath();
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
			MultiValueMap<PersistentPropertyPathExtension, RelationValue> relations =
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
		Map<PersistentPropertyPathExtension, EntityPathRelations> relationEntityPaths
	) throws SQLException {

		MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> relationEntities = rootRow.getRelations();

		for (Map.Entry<PersistentPropertyPathExtension, EntityPathRelations> relationEntityPath
			: relationEntityPaths.entrySet()) {

			PersistentPropertyPathExtension relationPath = relationEntityPath.getKey();
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
		PersistentPropertyPathExtension relationPath,
		Identifier identifier,
		Object relationEntityId,
		Map<PersistentPropertyPathExtension, EntityPathRelations> nestedRelations
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
		@SuppressWarnings("unchecked")
		MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext =
			(MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty>)
				this.getMappingContext();
		PersistentPropertyPathExtension rootPath = new PersistentPropertyPathExtension(mappingContext, entity);
		return this.getEntityPathRelations(rootPath);
	}

	private EntityPathRelations getEntityPathRelations(PersistentPropertyPathExtension entityPath) {
		Map<PersistentPropertyPathExtension, EntityPathRelations> relations = new HashMap<>();

		@SuppressWarnings("unchecked")
		MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext =
			(MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty>)
				this.getMappingContext();

		for (RelationalPersistentProperty property : entityPath.getLeafEntity()) {
			if (property.isEmbedded()) {
				continue;
			}

			if (property.isEntity()
				|| (property.isCollectionLike() && property.isEntity())
				|| property.isMap()) {
				PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath =
					entityPath.extendBy(property).getRequiredPersistentPropertyPath();
				PersistentPropertyPathExtension relationPath = new PersistentPropertyPathExtension(
					mappingContext, propertyPath);
				EntityPathRelations entityPathRelations = this.getEntityPathRelations(relationPath);
				relations.put(relationPath, entityPathRelations);
			}
		}

		return new EntityPathRelations(entityPath, relations);
	}

	private MultiValueMap<PersistentPropertyPathExtension, RelationValue> accumulateRelations(
		MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> extractedRows) {

		MultiValueMap<PersistentPropertyPathExtension, RelationValue> relations = new LinkedMultiValueMap<>();
		for (Map.Entry<PersistentPropertyPathExtension, List<ExtractedRow>> extractedRow
			: extractedRows.entrySet()) {

			PersistentPropertyPathExtension path = extractedRow.getKey();
			List<ExtractedRow> rowValues = extractedRow.getValue();
			for (ExtractedRow rowValue : rowValues) {
				relations.add(
					path,
					new RelationValue(
						rowValue.getParentId(),
						rowValue.getRootId(),
						rowValue.getKeyValue(),
						rowValue.getRoot()));
				relations.addAll(this.accumulateRelations(rowValue.getRelations()));
			}
		}
		return relations;
	}

	private void setEntityRelations(
		Map<String, Object> rootEntity,
		RelationalPersistentEntity<?> persistentEntity,
		MultiValueMap<PersistentPropertyPathExtension, RelationValue> relationValues) {

		for (Map.Entry<PersistentPropertyPathExtension, List<RelationValue>> relations
			: relationValues.entrySet()) {

			PersistentPropertyPathExtension propertyPath = relations.getKey();
			RelationalPersistentProperty property =
				propertyPath.getRequiredPersistentPropertyPath().getLeafProperty();

			// Set relations for root entity
			if (persistentEntity.getType() == property.getOwner().getType()) {
				rootEntity.put(property.getName(), this.determineRelationValue(
					property, relations.getValue()));
			} else {
				Map<Object, RelationValue> parentValues = relationValues.getOrDefault(
					propertyPath.getParentPath(), new ArrayList<>()).stream()
					.collect(toMap(RelationValue::getValueId, it -> it));

				MultiValueMap<Object, RelationValue> parentKeyChildren = new LinkedMultiValueMap<>();
				for (RelationValue value : relations.getValue()) {
					parentKeyChildren.add(value.getParentId(), value);
				}

				for (Map.Entry<Object, List<RelationValue>> parentKeyChild
					: parentKeyChildren.entrySet()) {

					Map<String, Object> parentValue = parentValues.get(
						parentKeyChild.getKey()).getValue();
					List<RelationValue> relationValue = parentKeyChild.getValue();
					parentValue.put(
						property.getName(),
						this.determineRelationValue(property, relationValue));
				}
			}
		}
	}

	private Object getRootId(
		ResultSetHolder resultSet,
		RelationalPersistentEntity<?> entity) throws SQLException {

		return resultSet.getResultSet().getObject(entity.getIdColumn()
			.getReference(identifierProcessing));
	}

	private Identifier getRelationEntityIdentifier(
		PersistentPropertyPathExtension relationPath,
		RelationalPersistentEntity<?> entity,
		Map<String, Object> entityMap) {

		Object id = entityMap.get(entity.getRequiredIdProperty().getName());
		return Identifier.of(relationPath.getReverseColumnName(), id, Object.class);
	}

	protected String getIdColumnAlias(PersistentPropertyPathExtension relationPath) {
		return PropertyPathUtils.getColumnAlias(
			relationPath.extendBy(relationPath.getLeafEntity().getRequiredIdProperty())
		).getReference(identifierProcessing);
	}

	protected String getQualifierColumnAlias(PersistentPropertyPathExtension relationPath) {
		return PropertyPathUtils.getTableAlias(relationPath).getReference(identifierProcessing)
			+ "_" + relationPath.getQualifierColumn().getReference(identifierProcessing);
	}

	private Object determineRelationValue(
		RelationalPersistentProperty property,
		List<RelationValue> relationValue) {

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
		Object getParameterValue(PreferredConstructor.Parameter<?, P> parameter);
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
		private final PersistentPropertyPathExtension rootPath;
		private final Map<PersistentPropertyPathExtension, EntityPathRelations> relations;

		/**
		 * Instantiates a new Entity path relations.
		 *
		 * @param rootPath  the root path
		 * @param relations the relations
		 */
		EntityPathRelations(
			PersistentPropertyPathExtension rootPath,
			Map<PersistentPropertyPathExtension, EntityPathRelations> relations) {

			this.rootPath = rootPath;
			this.relations = relations;
		}

		/**
		 * Gets root path.
		 *
		 * @return the root path
		 */
		public PersistentPropertyPathExtension getRootPath() {
			return this.rootPath;
		}

		/**
		 * Gets relations.
		 *
		 * @return the relations
		 */
		public Map<PersistentPropertyPathExtension, EntityPathRelations> getRelations() {
			return this.relations;
		}
	}

	private static class ExtractedRow {
		private final Object parentId;
		private final RelationalPersistentEntity<?> rootEntity;
		private final Map<String, Object> root;
		private final Object rootId;
		private final Object keyValue;
		private final MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> relations;

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
			MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> relations) {

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
		public MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> getRelations() {
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
			Map<String, Object> value) {

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
	}

	private class SingleTableMapReadingContext {

		private final RelationalPersistentEntity<?> entity;

		private final PersistentPropertyPathExtension rootPath;
		private final PersistentPropertyPathExtension path;
		private final Identifier identifier;
		private final Object key;

		private final JdbcPropertyValueProvider propertyValueProvider;
		private final JdbcBackReferencePropertyValueProvider backReferencePropertyValueProvider;
		private final ResultSetAccessor accessor;

		private SingleTableMapReadingContext(
			PersistentPropertyPathExtension rootPath,
			ResultSetAccessor accessor,
			Identifier identifier,
			Object key) {

			RelationalPersistentEntity<?> entity = rootPath.getLeafEntity();

			Assert.notNull(entity, "The rootPath must point to an entity.");

			this.entity = entity;
			this.rootPath = rootPath;
			this.path = new PersistentPropertyPathExtension(getMappingContext(), this.entity);
			this.identifier = identifier;
			this.key = key;
			this.propertyValueProvider = new JdbcPropertyValueProvider(
				identifierProcessing, path, accessor);
			this.backReferencePropertyValueProvider = new JdbcBackReferencePropertyValueProvider(
				identifierProcessing, path,
				accessor);
			this.accessor = accessor;
		}

		private SingleTableMapReadingContext(
			RelationalPersistentEntity<?> entity,
			ResultSetAccessor accessor,
			PersistentPropertyPathExtension rootPath,
			PersistentPropertyPathExtension path,
			Identifier identifier, Object key) {

			this.entity = entity;
			this.rootPath = rootPath;
			this.path = path;
			this.identifier = identifier;
			this.key = key;
			this.propertyValueProvider = new JdbcPropertyValueProvider(
				identifierProcessing, path, accessor);
			this.backReferencePropertyValueProvider = new JdbcBackReferencePropertyValueProvider(
				identifierProcessing, path, accessor);
			this.accessor = accessor;
		}

		private SingleTableMapReadingContext(
			RelationalPersistentEntity<?> entity, PersistentPropertyPathExtension rootPath,
			PersistentPropertyPathExtension path, Identifier identifier, Object key,
			JdbcPropertyValueProvider propertyValueProvider,
			JdbcBackReferencePropertyValueProvider backReferencePropertyValueProvider,
			ResultSetAccessor accessor) {

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
				/* rootPath.extendBy(property)*/ rootPath, path.extendBy(property), identifier, key,
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
			for (RelationalPersistentProperty property : entity) {
				// skip absent simple properties
				if (isSimpleProperty(property)) {

					if (!propertyValueProvider.hasProperty(property)) {
						continue;
					}
				}

				Object value = readOrLoadProperty(idValue, property);
				map.put(property.getName(), value);
			}
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

			RelationalPersistentEntity<?> persistentEntity = path.getLeafEntity();

			Assert.state(persistentEntity != null, "Entity must not be null");

			for (RelationalPersistentProperty embeddedProperty : persistentEntity) {

				// if the embedded contains Lists, Sets or Maps we consider it non-empty
				if (embeddedProperty.isQualified() || embeddedProperty.isReference()) {
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
		private final PersistentPropertyPathExtension path;

		@SuppressWarnings("unchecked")
		private MapReadingContext(
			PersistentPropertyPathExtension rootPath,
			@Nullable Map<String, Object> entityMap) {

			RelationalPersistentEntity<T> entity = (RelationalPersistentEntity<T>)rootPath.getLeafEntity();

			Assert.notNull(entity, "The rootPath must point to an entity.");

			this.entity = entity;
			this.entityMap = entityMap;
			this.path = new PersistentPropertyPathExtension(getMappingContext(), this.entity);
		}

		private MapReadingContext(
			RelationalPersistentEntity<T> entity,
			@Nullable Map<String, Object> entityMap,
			PersistentPropertyPathExtension path) {

			this.entity = entity;
			this.entityMap = entityMap;
			this.path = path;
		}

		private <S> MapReadingContext<S> extendEntityBy(
			RelationalPersistentProperty property,
			@Nullable Map<String, Object> entityMap) {

			return new MapReadingContext(
				getMappingContext().getRequiredPersistentEntity(property.getActualType()),
				entityMap, path.extendBy(property));
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

			PreferredConstructor<T, RelationalPersistentProperty> persistenceConstructor =
				entity.getPersistenceConstructor();

			for (RelationalPersistentProperty property : entity) {

				if (persistenceConstructor != null
					&& persistenceConstructor.isConstructorParameter(property)) {

					continue;
				}

				// skip absent simple properties
				if (isSimpleProperty(property)) {

					if (this.entityMap == null
						| !this.entityMap.containsKey(property.getName())) {

						continue;
					}
				}

				Object value = readOrLoadProperty(idValue, property);
				propertyAccessor.setProperty(property, value);
			}

			return propertyAccessor.getBean();
		}

		@Nullable
		private Object readOrLoadProperty(
			@Nullable Object id, RelationalPersistentProperty property) {

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

		@Nullable
		private Object readEmbeddedEntityFrom(
			@Nullable Object idValue, RelationalPersistentProperty property) {

			Object value = this.entityMap.get(property.getName());
			MapReadingContext<?> newContext = extendEntityBy(property, (Map<String, Object>)value);

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

			RelationalPersistentEntity<?> persistentEntity = path.getLeafEntity();

			Assert.state(persistentEntity != null, "Entity must not be null");

			for (RelationalPersistentProperty embeddedProperty : persistentEntity) {

				// if the embedded contains Lists, Sets or Maps we consider it non-empty
				if (embeddedProperty.isQualified() || embeddedProperty.isReference()) {
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
			@Nullable Map<String, Object> value) {

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

			PreferredConstructor<?, RelationalPersistentProperty> persistenceConstructor = entity.getPersistenceConstructor();
			ParameterValueProvider<RelationalPersistentProperty> provider;

			if (persistenceConstructor != null && persistenceConstructor.hasParameters()) {
				SpELExpressionEvaluator expressionEvaluator = new DefaultSpELExpressionEvaluator(this.entityMap, spElContext);
				provider = new SpELExpressionParameterValueProvider<>(expressionEvaluator, getConversionService(),
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
			 * @see org.springframework.data.mapping.model.ParameterValueProvider#getParameterValue(org.springframework.data.mapping.PreferredConstructor.Parameter)
			 */
			@Override
			@Nullable
			public <T> T getParameterValue(PreferredConstructor.Parameter<T, RelationalPersistentProperty> parameter) {

				String parameterName = parameter.getName();

				Assert.notNull(parameterName, "A constructor parameter name must not be null to be used with Spring Data JDBC");

				RelationalPersistentProperty property = entity.getRequiredPersistentProperty(parameterName);
				return (T)readOrLoadProperty(idValue, property);
			}
		}
	}

	enum NoOpParameterValueProvider implements ParameterValueProvider<RelationalPersistentProperty> {

		INSTANCE;

		@Override
		public <T> T getParameterValue(PreferredConstructor.Parameter<T, RelationalPersistentProperty> parameter) {
			return null;
		}
	}
}
