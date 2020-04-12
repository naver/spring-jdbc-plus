package com.navercorp.spring.data.jdbc.plus.sql.convert;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.springframework.data.convert.CustomConversions;
import org.springframework.data.jdbc.core.convert.BasicJdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.KotlinDefaultMask;
import org.springframework.data.mapping.model.MappingInstantiationException;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.domain.Identifier;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.data.util.Streamable;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.ReflectJvmMapping;

public class AggregateResultJdbcConverter extends BasicJdbcConverter {
	private final IdentifierProcessing identifierProcessing;

	public AggregateResultJdbcConverter(
		MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> context,
		RelationResolver relationResolver) {

		super(context, relationResolver);
		this.identifierProcessing = IdentifierProcessing.ANSI;
	}

	public AggregateResultJdbcConverter(
		MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> context,
		RelationResolver relationResolver,
		CustomConversions conversions,
		JdbcTypeFactory typeFactory,
		IdentifierProcessing identifierProcessing) {

		super(context, relationResolver, conversions, typeFactory, identifierProcessing);
		this.identifierProcessing = identifierProcessing;
	}

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

	// Single Table ResultSet to Map
	@SuppressWarnings("unchecked")
	protected Map<String, Object> mapSingleTableRow(RelationalPersistentEntity<?> entity, ResultSet resultSet) {
		return new SingleTableMapReadingContext(new PersistentPropertyPathExtension(getMappingContext(), entity),
			new ResultSetAccessor(resultSet), Identifier.empty(), null).mapRow();
	}

	// Single Table ResultSet to Map
	protected Map<String, Object> mapSingleTableRow(PersistentPropertyPathExtension path, ResultSet resultSet, Identifier identifier) {
		return new SingleTableMapReadingContext(
			path.getLeafEntity(), new ResultSetAccessor(resultSet), path.getParentPath(), path, identifier, null).mapRow();
	}

	// Single Table ResultSet to Key, Map(Value)
	protected Map.Entry<Object, Map<String, Object>> mapSingleTableMapRow(
		PersistentPropertyPathExtension path, ResultSet resultSet, Identifier identifier, Object key) {
		Map<String, Object> mapValue = new SingleTableMapReadingContext(
			path.getLeafEntity(), new ResultSetAccessor(resultSet), path.getParentPath(), path, identifier, key).mapRow();
		return new HashMap.SimpleEntry<>(key, mapValue);
	}

	@SuppressWarnings("unchecked")
	protected <T> T mapAggregate(RelationalPersistentEntity<T> entity, Map<String, Object> aggregateMap) {
		return (T)new MapReadingContext<>(
			new PersistentPropertyPathExtension(getMappingContext(), entity),
			aggregateMap)
			.mapRow();
	}

	private List<Map<String, Object>> extractData(ResultSetHolder resultSetHolder, EntityPathRelations entityPathRelations) throws SQLException {
		PersistentPropertyPathExtension rootPath = entityPathRelations.getRootPath();
		RelationalPersistentEntity<?> persistentEntity = rootPath.getLeafEntity();

		Map<Object, ExtractedRow> extractedRows = new LinkedHashMap<>();

		while (!resultSetHolder.isDone() && resultSetHolder.next()) {
			Map<String, Object> entityMap = this.mapSingleTableRow(persistentEntity, resultSetHolder.getResultSet());

			Object rootId = this.getRootId(resultSetHolder, persistentEntity);
			ExtractedRow rootRow = extractedRows.get(rootId);
			if (rootRow == null) {
				rootRow = new ExtractedRow(null, persistentEntity, entityMap, rootId, null, new LinkedMultiValueMap<>());
				extractedRows.put(rootId, rootRow);
			}
			this.appendExtractRelationRows(resultSetHolder, rootRow, entityPathRelations.getRelations());
		}

		List<Map<String, Object>> result = new ArrayList<>(extractedRows.size());
		for (Map.Entry<Object, ExtractedRow> row : extractedRows.entrySet()) {
			ExtractedRow extractedRow = row.getValue();
			MultiValueMap<PersistentPropertyPathExtension, RelationValue> relations =
				this.accumulateRelations(extractedRow.getRelations());
			this.setEntityRelations(extractedRow.getRoot(), extractedRow.getRootEntity(), relations);
			result.add(extractedRow.getRoot());
		}

		return result;
	}

	private void appendExtractRelationRows(
		ResultSetHolder resultSetHolder,
		ExtractedRow rootRow,
		Map<PersistentPropertyPathExtension, EntityPathRelations> relationEntityPaths) throws SQLException {

		MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> relationEntities = rootRow.getRelations();

		for (Map.Entry<PersistentPropertyPathExtension, EntityPathRelations> relationEntityPath : relationEntityPaths.entrySet()) {
			PersistentPropertyPathExtension relationPath = relationEntityPath.getKey();
			List<ExtractedRow> relationRows = relationEntities.get(relationPath);

			// extract relation entity Id
			String idColumnAlias = this.getIdColumnAlias(relationEntityPath.getKey());
			Object relationEntityId = resultSetHolder.getResultSet().getObject(idColumnAlias);

			if (relationEntityId == null) {
				continue;
			}

			Identifier identifier = this.getRelationEntityIdentifier(relationPath, rootRow.getRootEntity(), rootRow.getRoot());

			if (CollectionUtils.isEmpty(relationRows)) {
				// First relation extract
				ExtractedRow extractedRow = this.extractRelationRow(
					resultSetHolder, rootRow.getRootId(), relationPath, identifier, relationEntityId, relationEntityPath.getValue().getRelations());

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
				this.appendExtractRelationRows(resultSetHolder, existRow, relationEntityPath.getValue().getRelations());
			} else {
				// extract new relation rows
				ExtractedRow relationExtractedRows = this.extractRelationRow(
					resultSetHolder, rootRow.getRootId(), relationPath, identifier, relationEntityId, relationEntityPath.getValue().getRelations());
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
		Map<PersistentPropertyPathExtension, EntityPathRelations> nestedRelations) throws SQLException {

		Map<String, Object> relationValue;
		Object key = null;
		if (relationPath.isMap()) {
			String keyColumn = this.getQualifierColumnAlias(relationPath);
			key = resultSetHolder.getResultSet().getObject(keyColumn);
			Map.Entry<Object, Map<String, Object>> relationMapEntry = this.mapSingleTableMapRow(
				relationPath, resultSetHolder.getResultSet(), identifier, key);
			relationValue = relationMapEntry.getValue();
		} else {
			relationValue = this.mapSingleTableRow(relationPath, resultSetHolder.getResultSet(), identifier);
		}

		ExtractedRow extractedRow = new ExtractedRow(
			rootId, relationPath.getLeafEntity(), relationValue, relationEntityId, key, new LinkedMultiValueMap<>());
		this.appendExtractRelationRows(resultSetHolder, extractedRow, nestedRelations);
		return extractedRow;
	}

	private EntityPathRelations getEntityPathRelations(RelationalPersistentEntity<?> entity) {
		@SuppressWarnings("unchecked")
		MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext =
			(MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty>)this.getMappingContext();
		PersistentPropertyPathExtension rootPath = new PersistentPropertyPathExtension(mappingContext, entity);
		return this.getEntityPathRelations(rootPath);
	}

	private EntityPathRelations getEntityPathRelations(PersistentPropertyPathExtension entityPath) {
		Map<PersistentPropertyPathExtension, EntityPathRelations> relations = new HashMap<>();

		@SuppressWarnings("unchecked")
		MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext =
			(MappingContext<RelationalPersistentEntity<?>, RelationalPersistentProperty>)this.getMappingContext();

		for (RelationalPersistentProperty property : entityPath.getLeafEntity()) {
			if (property.isEmbedded()) {
				continue;
			}

			if (property.isEntity() || (property.isCollectionLike() && property.isEntity()) || property.isMap()) {
				PersistentPropertyPath<? extends RelationalPersistentProperty> propertyPath = entityPath.extendBy(property)
					.getRequiredPersistentPropertyPath();
				PersistentPropertyPathExtension relationPath = new PersistentPropertyPathExtension(mappingContext, propertyPath);
				EntityPathRelations entityPathRelations = this.getEntityPathRelations(relationPath);
				relations.put(relationPath, entityPathRelations);
			}
		}

		return new EntityPathRelations(entityPath, relations);
	}

	private MultiValueMap<PersistentPropertyPathExtension, RelationValue> accumulateRelations(
		MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> extractedRows) {

		MultiValueMap<PersistentPropertyPathExtension, RelationValue> relations = new LinkedMultiValueMap<>();
		for (Map.Entry<PersistentPropertyPathExtension, List<ExtractedRow>> extractedRow : extractedRows.entrySet()) {
			PersistentPropertyPathExtension path = extractedRow.getKey();
			List<ExtractedRow> rowValues = extractedRow.getValue();
			for (ExtractedRow rowValue : rowValues) {
				relations.add(path, new RelationValue(rowValue.getParentId(), rowValue.getRootId(), rowValue.getKeyValue(), rowValue.getRoot()));
				relations.addAll(this.accumulateRelations(rowValue.getRelations()));
			}
		}
		return relations;
	}

	private void setEntityRelations(
		Map<String, Object> rootEntity,
		RelationalPersistentEntity<?> persistentEntity,
		MultiValueMap<PersistentPropertyPathExtension, RelationValue> relationValues) {

		for (Map.Entry<PersistentPropertyPathExtension, List<RelationValue>> relations : relationValues.entrySet()) {
			PersistentPropertyPathExtension propertyPath = relations.getKey();
			RelationalPersistentProperty property = propertyPath.getRequiredPersistentPropertyPath().getLeafProperty();

			// Set relations for root entity
			if (persistentEntity.getType() == property.getOwner().getType()) {
				rootEntity.put(property.getName(), this.determineRelationValue(property, relations.getValue()));
			} else {
				Map<Object, RelationValue> parentValues = relationValues.getOrDefault(propertyPath.getParentPath(), new ArrayList<>()).stream()
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

	private Object getRootId(ResultSetHolder resultSet, RelationalPersistentEntity<?> entity) throws SQLException {
		return resultSet.getResultSet().getObject(entity.getIdColumn()
			.getReference(identifierProcessing));
	}

	private Identifier getRelationEntityIdentifier(
		PersistentPropertyPathExtension relationPath, RelationalPersistentEntity<?> entity, Map<String, Object> entityMap) {

		Object id = entityMap.get(entity.getRequiredIdProperty().getName());
		return Identifier.of(relationPath.getReverseColumnName(), id, Object.class);
	}

	private String getIdColumnAlias(PersistentPropertyPathExtension relationPath) {
		return relationPath.extendBy(relationPath.getLeafEntity().getRequiredIdProperty())
			.getColumnAlias().getReference(identifierProcessing);
	}

	private String getQualifierColumnAlias(PersistentPropertyPathExtension relationPath) {
		return relationPath.getTableAlias().getReference(identifierProcessing)
			+ "_" + relationPath.getQualifierColumn().getReference(identifierProcessing);
	}

	private Object determineRelationValue(RelationalPersistentProperty property, List<RelationValue> relationValue) {
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
					"Could not mapping path %s from value %s. property is entity but multiple value.",
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
		return !property.isCollectionLike() && !property.isEntity() && !property.isMap() && !property.isEmbedded();
	}

	private static class ResultSetHolder {
		private ResultSet resultSet;
		private int currentRowNum;
		private boolean done;

		public ResultSetHolder(ResultSet resultSet) {
			this.resultSet = resultSet;
			this.currentRowNum = -1;
			this.done = false;
		}

		public boolean next() throws SQLException {
			boolean next = this.resultSet.next();
			this.currentRowNum++;
			if (!next) {
				this.done = true;
			}
			return next;
		}

		public boolean isDone() {
			return this.done;
		}

		public ResultSet getResultSet() {
			return this.resultSet;
		}

		public int getCurrentRowNum() {
			return this.currentRowNum;
		}
	}

	private static class EntityPathRelations {
		private final PersistentPropertyPathExtension rootPath;
		private final Map<PersistentPropertyPathExtension, EntityPathRelations> relations;

		EntityPathRelations(
			PersistentPropertyPathExtension rootPath,
			Map<PersistentPropertyPathExtension, EntityPathRelations> relations) {

			this.rootPath = rootPath;
			this.relations = relations;
		}

		public PersistentPropertyPathExtension getRootPath() {
			return this.rootPath;
		}

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

		public Object getParentId() {
			return this.parentId;
		}

		public RelationalPersistentEntity<?> getRootEntity() {
			return this.rootEntity;
		}

		public Map<String, Object> getRoot() {
			return this.root;
		}

		public Object getRootId() {
			return this.rootId;
		}

		public MultiValueMap<PersistentPropertyPathExtension, ExtractedRow> getRelations() {
			return this.relations;
		}

		public Object getKeyValue() {
			return this.keyValue;
		}
	}

	private static class RelationValue {
		private final Object parentId;
		private final Object valueId;
		private final Object keyValue;
		private final Map<String, Object> value;

		public RelationValue(Object parentId, Object valueId, Object keyValue, Map<String, Object> value) {
			this.parentId = parentId;
			this.valueId = valueId;
			this.keyValue = keyValue;
			this.value = value;
		}

		public Object getParentId() {
			return this.parentId;
		}

		public Object getValueId() {
			return this.valueId;
		}

		public Object getKeyValue() {
			return this.keyValue;
		}

		public Map<String, Object> getValue() {
			return this.value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			RelationValue that = (RelationValue)o;
			return Objects.equals(parentId, that.parentId) &&
				Objects.equals(valueId, that.valueId) &&
				Objects.equals(keyValue, that.keyValue) &&
				Objects.equals(value, that.value);
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

		private SingleTableMapReadingContext(PersistentPropertyPathExtension rootPath, ResultSetAccessor accessor, Identifier identifier,
			Object key) {

			RelationalPersistentEntity<?> entity = rootPath.getLeafEntity();

			Assert.notNull(entity, "The rootPath must point to an entity.");

			this.entity = entity;
			this.rootPath = rootPath;
			this.path = new PersistentPropertyPathExtension(getMappingContext(), this.entity);
			this.identifier = identifier;
			this.key = key;
			this.propertyValueProvider = new JdbcPropertyValueProvider(identifierProcessing, path, accessor);
			this.backReferencePropertyValueProvider = new JdbcBackReferencePropertyValueProvider(identifierProcessing, path,
				accessor);
		}

		private SingleTableMapReadingContext(RelationalPersistentEntity<?> entity, ResultSetAccessor accessor,
			PersistentPropertyPathExtension rootPath, PersistentPropertyPathExtension path,
			Identifier identifier, Object key) {

			this.entity = entity;
			this.rootPath = rootPath;
			this.path = path;
			this.identifier = identifier;
			this.key = key;
			this.propertyValueProvider = new JdbcPropertyValueProvider(identifierProcessing, path, accessor);
			this.backReferencePropertyValueProvider = new JdbcBackReferencePropertyValueProvider(identifierProcessing, path,
				accessor);
		}

		private SingleTableMapReadingContext(RelationalPersistentEntity<?> entity, PersistentPropertyPathExtension rootPath,
			PersistentPropertyPathExtension path, Identifier identifier, Object key,
			JdbcPropertyValueProvider propertyValueProvider,
			JdbcBackReferencePropertyValueProvider backReferencePropertyValueProvider) {

			this.entity = entity;
			this.rootPath = rootPath;
			this.path = path;
			this.identifier = identifier;
			this.key = key;

			this.propertyValueProvider = propertyValueProvider;
			this.backReferencePropertyValueProvider = backReferencePropertyValueProvider;
		}

		private SingleTableMapReadingContext extendBy(RelationalPersistentProperty property) {
			return new SingleTableMapReadingContext(
				getMappingContext().getRequiredPersistentEntity(property.getActualType()),
				/* rootPath.extendBy(property)*/ rootPath, path.extendBy(property), identifier, key,
				propertyValueProvider.extendBy(property), backReferencePropertyValueProvider.extendBy(property));
		}

		Map<String, Object> mapRow() {

			RelationalPersistentProperty idProperty = entity.getIdProperty();

			Object idValue = idProperty == null ? null : readFrom(idProperty);

			return createInstanceInternal(idValue);
		}

		private void populateProperties(Map<String, Object> map, @Nullable Object idValue) {
			PreferredConstructor<?, RelationalPersistentProperty> persistenceConstructor = entity.getPersistenceConstructor();

			for (RelationalPersistentProperty property : entity) {

				if (persistenceConstructor != null && persistenceConstructor.isConstructorParameter(property)) {
					continue;
				}

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
		private Object readOrLoadProperty(@Nullable Object id, RelationalPersistentProperty property) {
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
		private Object readEmbeddedEntityFrom(@Nullable Object idValue, RelationalPersistentProperty property) {

			SingleTableMapReadingContext newContext = extendBy(property);

			if (shouldCreateEmptyEmbeddedInstance(property) || newContext.hasInstanceValues(idValue)) {
				return newContext.createInstanceInternal(idValue);
			}

			return null;
		}

		private boolean shouldCreateEmptyEmbeddedInstance(RelationalPersistentProperty property) {
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
			RelationalPersistentEntity<?> entity = getMappingContext().getRequiredPersistentEntity(property.getActualType());
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
			Map<String, Object> instance = new MapGeneratingEntityInstantiator().createMap(
				entity, new ConvertingMapParameterValueProvider<>(parameter -> {
					String parameterName = parameter.getName();

					Assert.notNull(parameterName, "A constructor parameter name must not be null to be used with Spring Data JDBC");

					RelationalPersistentProperty property = entity.getRequiredPersistentProperty(parameterName);

					return readOrLoadProperty(idValue, property);
				}));
			populateProperties(instance, idValue);
			return instance;
		}

	}

	private class MapReadingContext<T> {

		private final RelationalPersistentEntity<T> entity;

		private final Map<String, Object> entityMap;
		private final PersistentPropertyPathExtension path;

		@SuppressWarnings("unchecked")
		private MapReadingContext(PersistentPropertyPathExtension rootPath, @Nullable Map<String, Object> entityMap) {

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

		private <S> MapReadingContext<S> extendEntityBy(RelationalPersistentProperty property, @Nullable Map<String, Object> entityMap) {
			return new MapReadingContext(
				getMappingContext().getRequiredPersistentEntity(property.getActualType()),
				entityMap, path.extendBy(property));
		}

		T mapRow() {

			RelationalPersistentProperty idProperty = entity.getIdProperty();

			Object idValue = idProperty == null ? null : readFrom(idProperty);

			return createInstanceInternal(idValue);
		}

		private T populateProperties(T instance, @Nullable Object idValue) {

			PersistentPropertyAccessor<T> propertyAccessor = getPropertyAccessor(entity, instance);

			PreferredConstructor<T, RelationalPersistentProperty> persistenceConstructor = entity.getPersistenceConstructor();

			for (RelationalPersistentProperty property : entity) {

				if (persistenceConstructor != null && persistenceConstructor.isConstructorParameter(property)) {
					continue;
				}

				// skip absent simple properties
				if (isSimpleProperty(property)) {

					if (this.entityMap == null || !this.entityMap.containsKey(property.getName())) {
						continue;
					}
				}

				Object value = readOrLoadProperty(idValue, property);
				propertyAccessor.setProperty(property, value);
			}

			return propertyAccessor.getBean();
		}

		@Nullable
		private Object readOrLoadProperty(@Nullable Object id, RelationalPersistentProperty property) {
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
		private Object readEmbeddedEntityFrom(@Nullable Object idValue, RelationalPersistentProperty property) {

			Object value = this.entityMap.get(property.getName());
			MapReadingContext<?> newContext = extendEntityBy(property, (Map<String, Object>)value);

			if (shouldCreateEmptyEmbeddedInstance(property) || newContext.hasInstanceValues(idValue)) {
				return newContext.createInstanceInternal(idValue);
			}

			return null;
		}

		private boolean shouldCreateEmptyEmbeddedInstance(RelationalPersistentProperty property) {
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
		private Object readEntityFrom(RelationalPersistentProperty property, @Nullable Map<String, Object> value) {
			if (value == null) {
				return null;
			}

			MapReadingContext<?> newContext = extendEntityBy(property, value);
			RelationalPersistentEntity<?> entity = getMappingContext().getRequiredPersistentEntity(property.getActualType());
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
			Map<Object, Map<String, Object>> mapValues = (Map<Object, Map<String, Object>>)this.entityMap.get(property.getName());
			if (mapValues == null) {
				return new HashMap<>();
			}

			return mapValues.entrySet().stream()
				.map(entry -> new HashMap.SimpleEntry<>(entry.getKey(), this.readEntityFrom(property, entry.getValue())))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		}

		private List<Object> readCollectionFrom(RelationalPersistentProperty property) {
			if (this.entityMap == null) {
				return new ArrayList<>();
			}

			@SuppressWarnings("unchecked")
			Iterable<Map<String, Object>> collectionValues = (Iterable<Map<String, Object>>)this.entityMap.get(property.getName());
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

			T instance = createInstance(entity, parameter -> {

				String parameterName = parameter.getName();

				Assert.notNull(parameterName, "A constructor parameter name must not be null to be used with Spring Data JDBC");

				RelationalPersistentProperty property = entity.getRequiredPersistentProperty(parameterName);

				return readOrLoadProperty(idValue, property);
			});
			return populateProperties(instance, idValue);
		}
	}

	protected final class ConvertingMapParameterValueProvider<P extends PersistentProperty<P>> implements MapParameterValueProvider<P> {
		private final Function<PreferredConstructor.Parameter<?, P>, Object> delegate;

		public ConvertingMapParameterValueProvider(Function<PreferredConstructor.Parameter<?, P>, Object> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object getParameterValue(PreferredConstructor.Parameter<?, P> parameter) {
			Object value = delegate.apply(parameter);
			TypeInformation typeInformation = parameter.getType();

			if (value != null
				&& Map.class.isAssignableFrom(value.getClass())
				&& !typeInformation.getType().isAssignableFrom(value.getClass())) {
				return value;
			}

			return readValue(delegate.apply(parameter), typeInformation);
		}
	}

	protected final class MapGeneratingEntityInstantiator {
		public MapGeneratingEntityInstantiator() {
		}

		public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> Map<String, Object> createMap(
			E entity, MapParameterValueProvider<P> provider) {

			PreferredConstructor<?, ?> constructor = entity.getPersistenceConstructor();

			if (ReflectionUtils.isSupportedKotlinClass(entity.getType()) && constructor != null) {

				PreferredConstructor<?, ?> defaultConstructor = new DefaultingKotlinConstructorResolver(entity)
					.getDefaultConstructor();

				if (defaultConstructor != null) {

					MapInstantiator instantiator = new MapInstantiator(constructor);

					return new KotlinMapInstantiatorAdapter(instantiator, constructor).createInstance(entity, provider);
				}
			}

			return new MapInstantiatorAdapter(new MapInstantiator(constructor)).createInstance(entity, provider);
		}
	}

	protected interface MapParameterValueProvider<P extends PersistentProperty<P>> {
		@Nullable
		Object getParameterValue(PreferredConstructor.Parameter<?, P> parameter);
	}

	private static class MapInstantiator {
		private final PreferredConstructor<?, ?> constructor;

		public MapInstantiator(PreferredConstructor<?, ?> constructor) {
			this.constructor = constructor;
		}

		public Map<String, Object> newInstance(Object... args) {
			Map<String, Object> result = new HashMap<>();
			List<? extends PreferredConstructor.Parameter<Object, ?>> parameters = constructor.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				result.put(parameters.get(i).getName(), args[i]);
			}
			return result;
		}
	}

	private static class MapInstantiatorAdapter {

		private final MapInstantiator instantiator;

		public MapInstantiatorAdapter(MapInstantiator instantiator) {
			this.instantiator = instantiator;
		}

		public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> Map<String, Object> createInstance(
			E entity, MapParameterValueProvider<P> provider) {

			Object[] params = extractInvocationArguments(entity.getPersistenceConstructor(), provider);

			try {
				return instantiator.newInstance(params);
			} catch (Exception e) {
				throw new MappingInstantiationException(entity, Arrays.asList(params), e);
			}
		}

		private <P extends PersistentProperty<P>, T> Object[] extractInvocationArguments(
			@Nullable PreferredConstructor<? extends T, P> constructor, MapParameterValueProvider<P> provider) {

			if (constructor == null || !constructor.hasParameters()) {
				return allocateArguments(0);
			}

			Object[] params = allocateArguments(constructor.getConstructor().getParameterCount());

			int index = 0;
			for (PreferredConstructor.Parameter<?, P> parameter : constructor.getParameters()) {
				params[index++] = provider.getParameterValue(parameter);
			}

			return params;
		}
	}

	private static class DefaultingKotlinConstructorResolver {

		private final @Nullable
		PreferredConstructor<?, ?> defaultConstructor;

		@SuppressWarnings("unchecked")
		DefaultingKotlinConstructorResolver(PersistentEntity<?, ?> entity) {

			Constructor<?> hit = resolveDefaultConstructor(entity);
			PreferredConstructor<?, ?> persistenceConstructor = entity.getPersistenceConstructor();

			if (hit != null && persistenceConstructor != null) {
				this.defaultConstructor = new PreferredConstructor<>(hit,
					persistenceConstructor.getParameters().toArray(new PreferredConstructor.Parameter[0]));
			} else {
				this.defaultConstructor = null;
			}
		}

		@Nullable
		private static Constructor<?> resolveDefaultConstructor(PersistentEntity<?, ?> entity) {

			PreferredConstructor<?, ?> persistenceConstructor = entity.getPersistenceConstructor();

			if (persistenceConstructor == null) {
				return null;
			}

			Constructor<?> hit = null;
			Constructor<?> constructor = persistenceConstructor.getConstructor();

			for (Constructor<?> candidate : entity.getType().getDeclaredConstructors()) {

				// use only synthetic constructors
				if (!candidate.isSynthetic()) {
					continue;
				}

				// candidates must contain at least two additional parameters (int, DefaultConstructorMarker).
				// Number of defaulting masks derives from the original constructor arg count
				int syntheticParameters = KotlinDefaultMask.getMaskCount(constructor.getParameterCount())
					+ /* DefaultConstructorMarker */ 1;

				if (constructor.getParameterCount() + syntheticParameters != candidate.getParameterCount()) {
					continue;
				}

				java.lang.reflect.Parameter[] constructorParameters = constructor.getParameters();
				java.lang.reflect.Parameter[] candidateParameters = candidate.getParameters();

				if (!candidateParameters[candidateParameters.length - 1].getType().getName()
					.equals("kotlin.jvm.internal.DefaultConstructorMarker")) {
					continue;
				}

				if (parametersMatch(constructorParameters, candidateParameters)) {
					hit = candidate;
					break;
				}
			}

			return hit;
		}

		private static boolean parametersMatch(java.lang.reflect.Parameter[] constructorParameters,
			java.lang.reflect.Parameter[] candidateParameters) {

			return IntStream.range(0, constructorParameters.length)
				.allMatch(i -> constructorParameters[i].getType().equals(candidateParameters[i].getType()));
		}

		@Nullable
		PreferredConstructor<?, ?> getDefaultConstructor() {
			return defaultConstructor;
		}
	}

	private static class KotlinMapInstantiatorAdapter {

		private final MapInstantiator instantiator;
		private final KFunction<?> constructor;
		private final List<KParameter> kParameters;
		private final Constructor<?> synthetic;

		KotlinMapInstantiatorAdapter(MapInstantiator instantiator, PreferredConstructor<?, ?> constructor) {

			KFunction<?> kotlinConstructor = ReflectJvmMapping.getKotlinFunction(constructor.getConstructor());

			if (kotlinConstructor == null) {
				throw new IllegalArgumentException(
					"No corresponding Kotlin constructor found for " + constructor.getConstructor());
			}

			this.instantiator = instantiator;
			this.constructor = kotlinConstructor;
			this.kParameters = kotlinConstructor.getParameters();
			this.synthetic = constructor.getConstructor();
		}

		public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> Map<String, Object> createInstance(
			E entity, MapParameterValueProvider<P> provider) {

			Object[] params = extractInvocationArguments(entity.getPersistenceConstructor(), provider);

			try {
				return instantiator.newInstance(params);
			} catch (Exception e) {
				throw new MappingInstantiationException(entity, Arrays.asList(params), e);
			}
		}

		private <P extends PersistentProperty<P>, T> Object[] extractInvocationArguments(
			@Nullable PreferredConstructor<? extends T, P> preferredConstructor, MapParameterValueProvider<P> provider) {

			if (preferredConstructor == null) {
				throw new IllegalArgumentException("PreferredConstructor must not be null!");
			}

			Object[] params = allocateArguments(
				synthetic.getParameterCount() + KotlinDefaultMask.getMaskCount(synthetic.getParameterCount()) + /* DefaultConstructorMarker */1);
			int userParameterCount = kParameters.size();

			List<PreferredConstructor.Parameter<Object, P>> parameters = preferredConstructor.getParameters();

			// Prepare user-space arguments
			for (int i = 0; i < userParameterCount; i++) {

				PreferredConstructor.Parameter<Object, P> parameter = parameters.get(i);
				params[i] = provider.getParameterValue(parameter);
			}

			KotlinDefaultMask defaultMask = KotlinDefaultMask.from(constructor, it -> {

				int index = kParameters.indexOf(it);

				PreferredConstructor.Parameter<Object, P> parameter = parameters.get(index);
				Class<Object> type = parameter.getType().getType();

				if (it.isOptional() && params[index] == null) {
					if (type.isPrimitive()) {

						// apply primitive defaulting to prevent NPE on primitive downcast
						params[index] = ReflectionUtils.getPrimitiveDefault(type);
					}
					return false;
				}

				return true;
			});

			int[] defaulting = defaultMask.getDefaulting();
			// append nullability masks to creation arguments
			for (int i = 0; i < defaulting.length; i++) {
				params[userParameterCount + i] = defaulting[i];
			}

			return params;
		}
	}

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static Object[] allocateArguments(int argumentCount) {
		return argumentCount == 0 ? EMPTY_ARGS : new Object[argumentCount];
	}

}
