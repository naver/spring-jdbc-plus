package com.navercorp.spring.data.jdbc.plus.sql.convert;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.relational.core.mapping.Embedded.*;
import static org.springframework.data.relational.core.sql.SqlIdentifier.quoted;
import static org.springframework.data.relational.core.sql.SqlIdentifier.unquoted;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.convert.BasicJdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
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
	private RelationalMappingContext context = new JdbcMappingContext();
	JdbcConverter converter = new BasicJdbcConverter(context, (identifier, path) -> {
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
		final String sql = sqlGenerator.getFindOne();

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(sql).startsWith("SELECT") //
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
		});
	}

	@Test // DATAJDBC-111
	public void findAll() {
		final String sql = sqlGenerator.getFindAll();

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(sql).startsWith("SELECT") //
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
		});
	}

	@Test // DATAJDBC-111
	public void findAllInList() {
		final String sql = sqlGenerator.getFindAllInList();

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(sql).startsWith("SELECT") //
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
		});
	}

	@Test // DATAJDBC-111
	public void insert() {
		final String sql = sqlGenerator.getInsert(emptySet());

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(sql) //
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
		});
	}

	@Test // DATAJDBC-111
	public void update() {
		final String sql = sqlGenerator.getUpdate();

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(sql) //
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
		});
	}

	@Test // DATAJDBC-340
	@Disabled // this is just broken right now
	public void deleteByPath() {

		final String sql = sqlGenerator
			.createDeleteByPath(PropertyPathTestingUtils
				.toPath("embedded.other", DummyEntity2.class, context));

		assertThat(sql).containsSequence("DELETE FROM other_entity", //
			"WHERE", //
			"embedded_with_reference IN (", //
			"SELECT ", //
			"id ", //
			"FROM", //
			"dummy_entity2", //
			"WHERE", //
			"embedded_with_reference = :rootId");
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

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(join.getJoinTable().getName())
				.isEqualTo(unquoted("other_entity"));
			softly.assertThat(join.getJoinColumn().getTable()).isEqualTo(join.getJoinTable());
			softly.assertThat(join.getJoinColumn().getName())
				.isEqualTo(unquoted("dummy_entity2"));
			softly.assertThat(join.getParentId().getName())
				.isEqualTo(unquoted("id"));
			softly.assertThat(join.getParentId().getTable().getName())
				.isEqualTo(unquoted("dummy_entity2"));
		});
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
		SoftAssertions.assertSoftly(softly -> {
			softly.assertThat(PropertyPathUtils.getTableAlias(extPath("prefixEmbeddable.other")))
				.isEqualTo(quoted("prefix_other"));
			softly.assertThat(PropertyPathUtils.getTableAlias(extPath("embeddable.other")))
				.isEqualTo(quoted("other"));
		});
	}

	private SqlGenerator.Join generateJoin(String path, Class<?> type) {
		return createSqlGenerator(type)
			.getJoin(new PersistentPropertyPathExtension(
				context, PropertyPathTestingUtils.toPath(path, type, context)));
	}

	@Nullable
	private SqlIdentifier getAlias(Object maybeAliased) {

		if (maybeAliased instanceof Aliased) {
			return ((Aliased)maybeAliased).getAlias();
		}
		return null;
	}

	private PersistentPropertyPathExtension extPath(RelationalPersistentEntity<?> entity) {
		return new PersistentPropertyPathExtension(context, entity);
	}
	private PersistentPropertyPathExtension extPath(String path) {
		return new PersistentPropertyPathExtension(context, createSimplePath(path));
	}

	PersistentPropertyPath<RelationalPersistentProperty> createSimplePath(String path) {
		return PropertyPathTestingUtils.toPath(path, DummyEntity3.class, context);
	}

	private org.springframework.data.relational.core.sql.Column generatedColumn(
		String path, Class<?> type) {

		return createSqlGenerator(type)
			.getColumn(new PersistentPropertyPathExtension(
				context, PropertyPathTestingUtils.toPath(path, type, context)));
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
		Embeddable prefixedEmbeddable;
		@Embedded(onEmpty = OnEmpty.USE_NULL)
		Embeddable embeddable;
	}

	@SuppressWarnings("unused")
	static class Embeddable {
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
