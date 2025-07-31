package com.navercorp.spring.data.jdbc.plus.support.convert;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.relational.core.mapping.Embedded.Nullable;
import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty;
import static org.springframework.data.relational.core.sql.SqlIdentifier.quoted;
import static org.springframework.data.relational.core.sql.SqlIdentifier.unquoted;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.Aliased;
import org.springframework.data.relational.core.sql.SqlIdentifier;

/**
 * COPY org.springframework.data.relational.core.convert.SqlGeneratorEmbeddedUnitTests
 *
 * @author Myeonghyeon Lee
 */
@SuppressWarnings("checkstyle:linelength")
class SqlGeneratorEmbeddedTest {
	private RelationalMappingContext context = JdbcMappingContext.forQuotedIdentifiers();
	JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
		throw new UnsupportedOperationException();
	});
	private SqlGenerator sqlGenerator;

	@BeforeEach
	public void setUp() {
		this.context.setForceQuote(false);
		this.sqlGenerator = createSqlGenerator(DummyEntity.class);
	}

	SqlGenerator createSqlGenerator(Class<?> type) {
		RelationalPersistentEntity<?> persistentEntity = context.getRequiredPersistentEntity(type);
		return new SqlGenerator(context, converter, persistentEntity, NonQuotingDialect.INSTANCE);
	}

	@Test // DATAJDBC-111
	public void findOne() {
		String sql = sqlGenerator.getFindOne();

		assertThat(sql).startsWith("SELECT") //
			.contains("dummy_entity.id1 AS id1") //
			.contains("dummy_entity.test AS test") //
			.contains("dummy_entity.attr1 AS attr1") //
			.contains("dummy_entity.attr2 AS attr2") //
			.contains("dummy_entity.prefix2_attr1 AS prefix2_attr1") //
			.contains("dummy_entity.prefix2_attr2 AS prefix2_attr2") //
			.contains("dummy_entity.prefix_test AS prefix_test") //
			.contains("dummy_entity.prefix_attr1 AS prefix_attr1") //
			.contains("dummy_entity.prefix_attr2 AS prefix_attr2") //
			.contains("dummy_entity.prefix_prefix2_attr1 AS prefix_prefix2_attr1") //
			.contains("dummy_entity.prefix_prefix2_attr2 AS prefix_prefix2_attr2") //
			.contains("WHERE dummy_entity.id1 = :id") //
			.doesNotContain("JOIN").doesNotContain("embeddable"); //
	}

	@Test // GH-574
	void findOneWrappedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithWrappedId.class);

		String sql = sqlGenerator.getFindOne();

		assertThat(sql).startsWith("SELECT") //
			.contains("with_wrapped_id.name AS name") //
			.contains("with_wrapped_id.id") //
			.contains("WHERE with_wrapped_id.id = :id");
	}

	@Test // GH-574
	void findOneEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);

		String sql = sqlGenerator.getFindOne();

		assertThat(sql).startsWith("SELECT") //
			.contains("with_embedded_id.name AS name") //
			.contains("with_embedded_id.one") //
			.contains("with_embedded_id.two") //
			.contains(" WHERE ") //
			.contains("with_embedded_id.one = :one") //
			.contains("with_embedded_id.two = :two");
	}

	@Test // GH-574
	void deleteByIdEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);

		String sql = sqlGenerator.getDeleteById();

		assertThat(sql).startsWith("DELETE") //
			.contains(" WHERE ") //
			.contains("with_embedded_id.one = :one") //
			.contains("with_embedded_id.two = :two");
	}

	@Test // GH-574
	void deleteByIdInEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);

		String sql = sqlGenerator.getDeleteByIdIn();

		assertThat(sql).startsWith("DELETE") //
			.contains(" WHERE ") //
			.contains("(with_embedded_id.one, with_embedded_id.two) IN (:ids)");
	}

	@Test // GH-574
	void deleteByPathEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);
		PersistentPropertyPath<RelationalPersistentProperty> path = PersistentPropertyPathTestUtils.getPath("other",
			WithEmbeddedIdAndReference.class, context);

		String sql = sqlGenerator.createDeleteByPath(path);

		assertThat(sql).startsWith("DELETE FROM other_entity WHERE") //
			.contains("other_entity.with_embedded_id_and_reference_one = :one") //
			.contains("other_entity.with_embedded_id_and_reference_two = :two");
	}

	@Test // GH-574
	void deleteInByPathEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);
		PersistentPropertyPath<RelationalPersistentProperty> path = PersistentPropertyPathTestUtils.getPath("other",
			WithEmbeddedIdAndReference.class, context);

		String sql = sqlGenerator.createDeleteInByPath(path);

		assertThat(sql).startsWith("DELETE FROM other_entity WHERE") //
			.contains(" WHERE ") //
			.contains(
				"(other_entity.with_embedded_id_and_reference_one, other_entity.with_embedded_id_and_reference_two) "
					+ "IN (:ids)");
	}

	@Test // GH-574
	void updateWithEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);

		String sql = sqlGenerator.getUpdate();

		assertThat(sql).startsWith("UPDATE") //
			.contains(" WHERE ") //
			.contains("with_embedded_id.one = :one") //
			.contains("with_embedded_id.two = :two");
	}

	@Test // GH-574
	void existsByIdEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);

		String sql = sqlGenerator.getExists();

		assertThat(sql).startsWith("SELECT COUNT") //
			.contains(" WHERE ") //
			.contains("with_embedded_id.one = :one") //
			.contains("with_embedded_id.two = :two");
	}

	@Test // DATAJDBC-111
	public void findAll() {
		String sql = sqlGenerator.getFindAll();

		assertThat(sql).startsWith("SELECT") //
			.contains("dummy_entity.id1 AS id1") //
			.contains("dummy_entity.test AS test") //
			.contains("dummy_entity.attr1 AS attr1") //
			.contains("dummy_entity.attr2 AS attr2") //
			.contains("dummy_entity.prefix2_attr1 AS prefix2_attr1") //
			.contains("dummy_entity.prefix2_attr2 AS prefix2_attr2") //
			.contains("dummy_entity.prefix_test AS prefix_test") //
			.contains("dummy_entity.prefix_attr1 AS prefix_attr1") //
			.contains("dummy_entity.prefix_attr2 AS prefix_attr2") //
			.contains("dummy_entity.prefix_prefix2_attr1 AS prefix_prefix2_attr1") //
			.contains("dummy_entity.prefix_prefix2_attr2 AS prefix_prefix2_attr2") //
			.doesNotContain("JOIN") //
			.doesNotContain("embeddable");
	}

	@Test // DATAJDBC-111
	public void findAllInList() {

		String sql = sqlGenerator.getFindAllInList();

		assertThat(sql).startsWith("SELECT") //
			.contains("dummy_entity.id1 AS id1") //
			.contains("dummy_entity.test AS test") //
			.contains("dummy_entity.attr1 AS attr1") //
			.contains("dummy_entity.attr2 AS attr2")
			.contains("dummy_entity.prefix2_attr1 AS prefix2_attr1") //
			.contains("dummy_entity.prefix2_attr2 AS prefix2_attr2") //
			.contains("dummy_entity.prefix_test AS prefix_test") //
			.contains("dummy_entity.prefix_attr1 AS prefix_attr1") //
			.contains("dummy_entity.prefix_attr2 AS prefix_attr2") //
			.contains("dummy_entity.prefix_prefix2_attr1 AS prefix_prefix2_attr1") //
			.contains("dummy_entity.prefix_prefix2_attr2 AS prefix_prefix2_attr2") //
			.contains("WHERE dummy_entity.id1 IN (:ids)") //
			.doesNotContain("JOIN") //
			.doesNotContain("embeddable");
	}

	@Test // GH-574
	void findAllInListEmbeddedId() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedId.class);

		String sql = sqlGenerator.getFindAllInList();

		assertThat(sql).startsWith("SELECT") //
			.contains("with_embedded_id.name AS name") //
			.contains("with_embedded_id.one") //
			.contains("with_embedded_id.two") //
			.contains(" WHERE (with_embedded_id.one, with_embedded_id.two) IN (:ids)");
	}

	@Test // GH-574
	void findOneWithReference() {

		SqlGenerator sqlGenerator = createSqlGenerator(WithEmbeddedIdAndReference.class);

		String sql = sqlGenerator.getFindOne();

		assertThat(sql).startsWith("SELECT") //
			.contains(" LEFT OUTER JOIN other_entity other ") //
			.contains(" ON ") //
			.contains(
				" other.with_embedded_id_and_reference_one = with_embedded_id_and_reference.one ") //
			.contains(
				" other.with_embedded_id_and_reference_two = with_embedded_id_and_reference.two ") //
			.contains(" WHERE ") //
			.contains("with_embedded_id_and_reference.one = :one") //
			.contains("with_embedded_id_and_reference.two = :two");
	}

	@Test // DATAJDBC-111
	public void insert() {
		final String sql = sqlGenerator.getInsert(emptySet());

		assertThat(sql) //
			.startsWith("INSERT INTO") //
			.contains("dummy_entity") //
			.contains(":test") //
			.contains(":attr1") //
			.contains(":attr2") //
			.contains(":prefix2_attr1") //
			.contains(":prefix2_attr2") //
			.contains(":prefix_test") //
			.contains(":prefix_attr1") //
			.contains(":prefix_attr2") //
			.contains(":prefix_prefix2_attr1") //
			.contains(":prefix_prefix2_attr2");
	}

	@Test // DATAJDBC-111
	public void update() {
		final String sql = sqlGenerator.getUpdate();

		assertThat(sql) //
			.startsWith("UPDATE") //
			.contains("dummy_entity") //
			.contains("test = :test") //
			.contains("attr1 = :attr1") //
			.contains("attr2 = :attr2") //
			.contains("prefix2_attr1 = :prefix2_attr1") //
			.contains("prefix2_attr2 = :prefix2_attr2") //
			.contains("prefix_test = :prefix_test") //
			.contains("prefix_attr1 = :prefix_attr1") //
			.contains("prefix_attr2 = :prefix_attr2") //
			.contains("prefix_prefix2_attr1 = :prefix_prefix2_attr1") //
			.contains("prefix_prefix2_attr2 = :prefix_prefix2_attr2");
	}

	@Test // DATAJDBC-340
	public void deleteByPath() {

		sqlGenerator = createSqlGenerator(DummyEntity2.class);

		final String sql = sqlGenerator
			.createDeleteByPath(PersistentPropertyPathTestUtils.getPath("embedded.other", DummyEntity2.class, context));
		;

		assertThat(sql).isEqualTo("DELETE FROM other_entity WHERE other_entity.dummy_entity2 = :id");
	}

	@Test // DATAJDBC-340
	public void noJoinForEmbedded() {

		SqlGenerator.Join join = generateJoin("embeddable", DummyEntity.class);

		assertThat(join).isNull();
	}

	@Test // DATAJDBC-340
	public void columnForEmbeddedProperty() {

		assertThat(generatedColumn("embeddable.test", DummyEntity.class)) //
			.extracting( //
				c -> c.getName(), //
				c -> c.getTable().getName(), //
				c -> getAlias(c.getTable()), //
				this::getAlias) //
			.containsExactly( //
				unquoted("test"), //
				unquoted("dummy_entity"), //
				null, //
				unquoted("test"));
	}

	@Test // DATAJDBC-340
	public void noColumnForEmbedded() {

		assertThat(generatedColumn("embeddable", DummyEntity.class)) //
			.isNull();
	}

	@Test // DATAJDBC-340
	public void noJoinForPrefixedEmbedded() {

		SqlGenerator.Join join = generateJoin("prefixedEmbeddable", DummyEntity.class);

		assertThat(join).isNull();
	}

	@Test // DATAJDBC-340
	public void columnForPrefixedEmbeddedProperty() {

		assertThat(generatedColumn("prefixedEmbeddable.test", DummyEntity.class)) //
			.extracting( //
				c -> c.getName(), //
				c -> c.getTable().getName(), //
				c -> getAlias(c.getTable()), //
				this::getAlias) //
			.containsExactly( //
				unquoted("prefix_test"), //
				unquoted("dummy_entity"), //
				null, //
				unquoted("prefix_test"));
	}

	@Test // DATAJDBC-340
	public void noJoinForCascadedEmbedded() {

		SqlGenerator.Join join = generateJoin("embeddable.embeddable", DummyEntity.class);

		assertThat(join).isNull();
	}

	@Test // DATAJDBC-340
	public void columnForCascadedEmbeddedProperty() {

		assertThat(generatedColumn("embeddable.embeddable.attr1", DummyEntity.class)) //
			.extracting(
				c -> c.getName(),
				c -> c.getTable().getName(),
				c -> getAlias(c.getTable()),
				this::getAlias)
			.containsExactly(unquoted("attr1"), unquoted("dummy_entity"), null,
				unquoted("attr1"));
	}

	@Test // DATAJDBC-340
	public void joinForEmbeddedWithReference() {

		SqlGenerator.Join join = generateJoin("embedded.other", DummyEntity2.class);

		assertThat(join.joinTable().getName()).isEqualTo(SqlIdentifier.unquoted("other_entity"));
		assertThat(join.condition())
			.isEqualTo(
				SqlGeneratorTest.equalsCondition("dummy_entity2", "id", join.joinTable(), "dummy_entity2"));
	}

	@Test // DATAJDBC-340
	public void columnForEmbeddedWithReferenceProperty() {

		assertThat(generatedColumn("embedded.other.value", DummyEntity2.class)) //
			.extracting( //
				c -> c.getName(), //
				c -> c.getTable().getName(), //
				c -> getAlias(c.getTable()), //
				this::getAlias) //
			.containsExactly( //
				unquoted("value"), //
				unquoted("other_entity"), //
				quoted("prefix_other"), //
				unquoted("prefix_other_value"));
	}

	@Test
	public void getTableAlias() {
		assertThat(PropertyPathUtils.getTableAlias(extPath("prefixEmbeddable.other")))
			.isEqualTo(quoted("prefix_other"));
		assertThat(PropertyPathUtils.getTableAlias(extPath("embeddable.other")))
			.isEqualTo(quoted("other"));
	}

	@Nullable
	private SqlGenerator.Join generateJoin(String path, Class<?> type) {
		return createSqlGenerator(type)
			.getJoin(context.getAggregatePath(PropertyPathTestingUtils.toPath(path, type, context)));
	}

	@Nullable
	private SqlIdentifier getAlias(Object maybeAliased) {

		if (maybeAliased instanceof Aliased aliased) {
			return aliased.getAlias();
		}
		return null;
	}

	private AggregatePath extPath(RelationalPersistentEntity<?> entity) {
		return context.getAggregatePath(entity);
	}

	private AggregatePath extPath(String path) {
		return context.getAggregatePath(createSimplePath(path));
	}

	PersistentPropertyPath<RelationalPersistentProperty> createSimplePath(String path) {
		return PropertyPathTestingUtils.toPath(path, DummyEntity3.class, context);
	}

	@Nullable
	private org.springframework.data.relational.core.sql.Column generatedColumn(
		String path, Class<?> type) {

		return createSqlGenerator(type)
			.getColumn(context.getAggregatePath(PropertyPathTestingUtils.toPath(path, type, context)));
	}

	record WrappedId(Long id) {
	}

	static class WithWrappedId {

		@Id
		@Embedded(onEmpty = OnEmpty.USE_NULL)
		WrappedId wrappedId;

		String name;
	}

	record EmbeddedId(Long one, String two) {
	}

	static class WithEmbeddedId {

		@Id
		@Embedded(onEmpty = OnEmpty.USE_NULL)
		EmbeddedId embeddedId;

		String name;

	}

	static class WithEmbeddedIdAndReference {

		@Id
		@Embedded(onEmpty = OnEmpty.USE_NULL)
		EmbeddedId embeddedId;

		String name;
		OtherEntity other;
	}

	@SuppressWarnings("unused")
	static class DummyEntity {

		@Column("id1")
		@Id
		Long id;

		@Embedded(onEmpty = OnEmpty.USE_NULL, prefix = "prefix_")
		CascadedEmbedded prefixedEmbeddable;

		@Embedded(onEmpty = OnEmpty.USE_NULL)
		CascadedEmbedded embeddable;
	}

	@SuppressWarnings("unused")
	static class CascadedEmbedded {
		String test;
		@Embedded(onEmpty = OnEmpty.USE_NULL, prefix = "prefix2_")
		NoId prefixedEmbeddable;
		@Embedded(onEmpty = OnEmpty.USE_NULL)
		NoId embeddable;
	}

	@SuppressWarnings("unused")
	static class NoId {
		Long attr1;
		String attr2;
	}

	@SuppressWarnings("unused")
	static class DummyEntity2 {

		@Id
		Long id;

		@Embedded(onEmpty = OnEmpty.USE_NULL, prefix = "prefix_")
		EmbeddedWithReference embedded;
	}

	static class DummyEntity3 {
		@Id
		Long id;

		@Embedded(onEmpty = OnEmpty.USE_NULL, prefix = "prefix_")
		EmbeddedWithReference prefixEmbeddable;

		@Embedded(onEmpty = OnEmpty.USE_NULL)
		EmbeddedWithReference embeddable;
	}

	static class EmbeddedWithReference {
		OtherEntity other;
	}

	static class OtherEntity {
		String value;
	}

}
