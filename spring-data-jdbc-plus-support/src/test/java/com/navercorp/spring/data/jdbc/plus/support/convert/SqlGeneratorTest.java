package com.navercorp.spring.data.jdbc.plus.support.convert;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.data.relational.core.sql.SqlIdentifier.quoted;
import static org.springframework.data.relational.core.sql.SqlIdentifier.unquoted;

import java.util.Map;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.convert.Identifier;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.core.dialect.JdbcSqlServerDialect;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.sql.Aliased;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.relational.core.sql.TableLike;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;
import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn.ValueType;
import com.navercorp.spring.jdbc.plus.commons.annotations.SqlTableAlias;

/**
 * COPY org.springframework.data.relational.core.convert.SqlGeneratorUnitTests
 *
 * @author Myeonghyeon Lee
 */
@SuppressWarnings("ALL")
class SqlGeneratorTest {
	static final Identifier BACKREF = Identifier.of(unquoted("backref"), "some-value", String.class);

	SqlGenerator sqlGenerator;
	NamingStrategy namingStrategy = new PrefixingNamingStrategy();
	RelationalMappingContext context = JdbcMappingContext.forQuotedIdentifiers(namingStrategy);
	JdbcConverter converter = new MappingJdbcConverter(context, (identifier, path) -> {
		throw new UnsupportedOperationException();
	});

	static Comparison equalsCondition(org.springframework.data.relational.core.sql.Table parentTable,
		SqlIdentifier parentId, org.springframework.data.relational.core.sql.Table joinedTable,
		SqlIdentifier joinedColumn) {
		return org.springframework.data.relational.core.sql.Column.create(joinedColumn, joinedTable)
			.isEqualTo(org.springframework.data.relational.core.sql.Column.create(parentId, parentTable));
	}

	static Comparison equalsCondition(SqlIdentifier parentTable, SqlIdentifier parentId,
		org.springframework.data.relational.core.sql.Table joinedTable,
		SqlIdentifier joinedColumn) {
		return equalsCondition(org.springframework.data.relational.core.sql.Table.create(parentTable), parentId,
			joinedTable, joinedColumn);
	}

	static Comparison equalsCondition(String parentTable, String parentId,
		org.springframework.data.relational.core.sql.Table joinedTable, String joinedColumn) {
		return equalsCondition(SqlIdentifier.unquoted(parentTable), SqlIdentifier.unquoted(parentId), joinedTable,
			SqlIdentifier.unquoted(joinedColumn));
	}

	@BeforeEach
	public void setUp() {
		this.sqlGenerator = createSqlGenerator(DummyEntity.class);
	}

	SqlGenerator createSqlGenerator(Class<?> type) {

		return createSqlGenerator(type, NonQuotingDialect.INSTANCE);
	}

	SqlGenerator createSqlGenerator(Class<?> type, Dialect dialect) {

		RelationalPersistentEntity<?> persistentEntity = context.getRequiredPersistentEntity(type);

		return new SqlGenerator(context, converter, persistentEntity, dialect);
	}

	@Test // DATAJDBC-112
	public void findOne() {

		String sql = sqlGenerator.getFindOne();

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(sql) //
			.startsWith("SELECT") //
			.contains("dummy_entity.id1 AS id1,") //
			.contains("dummy_entity.x_name AS x_name,") //
			.contains("dummy_entity.x_other AS x_other,") //
			.contains("ref.x_l1id AS ref_x_l1id") //
			.contains("ref.x_content AS ref_x_content").contains(" FROM dummy_entity") //
			.contains("ON ref.dummy_entity = dummy_entity.id1") //
			.contains("WHERE dummy_entity.id1 = :id") //
			// 1-N relationships do not get loaded via join
			.doesNotContain("Element AS elements");
		softAssertions.assertAll();
	}

	@Test // DATAJDBC-112
	public void cascadingDeleteFirstLevel() {

		String sql = sqlGenerator.createDeleteByPath(getPath("ref", DummyEntity.class));

		assertThat(sql).isEqualTo(
			"DELETE FROM referenced_entity WHERE referenced_entity.dummy_entity = :id1");
	}

	@Test // GH-537
	void cascadingDeleteInByPathFirstLevel() {

		String sql = sqlGenerator.createDeleteInByPath(getPath("ref", DummyEntity.class));

		assertThat(sql).isEqualTo("DELETE FROM referenced_entity WHERE referenced_entity.dummy_entity IN (:ids)");
	}

	@Test // DATAJDBC-112
	public void cascadingDeleteByPathSecondLevel() {

		String sql = sqlGenerator.createDeleteByPath(getPath("ref.further", DummyEntity.class));

		assertThat(sql).isEqualTo(
			"DELETE FROM second_level_referenced_entity "
				+ "WHERE second_level_referenced_entity.referenced_entity IN ("
				+ "SELECT referenced_entity.x_l1id FROM referenced_entity "
				+ "WHERE referenced_entity.dummy_entity = :id1)");
	}

	@Test // DATAJDBC-112
	public void deleteAll() {

		String sql = sqlGenerator.createDeleteAllSql(null);

		assertThat(sql).isEqualTo("DELETE FROM dummy_entity");
	}

	@Test // DATAJDBC-112
	public void cascadingDeleteAllFirstLevel() {

		String sql = sqlGenerator.createDeleteAllSql(getPath("ref", DummyEntity.class));

		assertThat(sql).isEqualTo(
			"DELETE FROM referenced_entity "
				+ "WHERE referenced_entity.dummy_entity IS NOT NULL");
	}

	@Test // DATAJDBC-112
	public void cascadingDeleteAllSecondLevel() {

		String sql = sqlGenerator.createDeleteAllSql(
			getPath("ref.further", DummyEntity.class));

		assertThat(sql).isEqualTo(
			"DELETE FROM second_level_referenced_entity "
				+ "WHERE second_level_referenced_entity.referenced_entity IN ("
				+ "SELECT referenced_entity.x_l1id FROM referenced_entity "
				+ "WHERE referenced_entity.dummy_entity IS NOT NULL)");
	}

	@Test // DATAJDBC-227
	public void deleteAllMap() {

		String sql = sqlGenerator.createDeleteAllSql(
			getPath("mappedElements", DummyEntity.class));

		assertThat(sql).isEqualTo("DELETE FROM element WHERE element.dummy_entity IS NOT NULL");
	}

	@Test // DATAJDBC-227
	public void deleteMapByPath() {

		String sql = sqlGenerator.createDeleteByPath(
			getPath("mappedElements", DummyEntity.class));

		assertThat(sql).isEqualTo("DELETE FROM element WHERE element.dummy_entity = :id1");
	}

	@Test // DATAJDBC-101
	public void findAllSortedByUnsorted() {

		String sql = sqlGenerator.getFindAll(Sort.unsorted());

		assertThat(sql).doesNotContain("ORDER BY");
	}

	@Test // DATAJDBC-101
	public void findAllSortedBySingleField() {

		String sql = sqlGenerator.getFindAll(Sort.by("name"));

		assertThat(sql).contains("SELECT", //
			"dummy_entity.id1 AS id1", //
			"dummy_entity.x_name AS x_name", //
			"dummy_entity.x_other AS x_other", //
			"ref.x_l1id AS ref_x_l1id", //
			"ref.x_content AS ref_x_content", //
			"ref_further.x_l2id AS ref_further_x_l2id", //
			"ref_further.x_something AS ref_further_x_something", //
			"FROM dummy_entity ", //
			"LEFT OUTER JOIN referenced_entity ref "
				+ "ON ref.dummy_entity = dummy_entity.id1", //
			"LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id", //
			"ORDER BY dummy_entity.x_name ASC");
	}

	@Test // DATAJDBC-101
	public void findAllSortedByMultipleFields() {

		String sql = sqlGenerator.getFindAll(
			Sort.by(new Sort.Order(
					Sort.Direction.DESC, "name"),
				new Sort.Order(Sort.Direction.ASC, "other")));

		assertThat(sql).contains("SELECT", //
			"dummy_entity.id1 AS id1", //
			"dummy_entity.x_name AS x_name", //
			"dummy_entity.x_other AS x_other", //
			"ref.x_l1id AS ref_x_l1id", //
			"ref.x_content AS ref_x_content", //
			"ref_further.x_l2id AS ref_further_x_l2id", //
			"ref_further.x_something AS ref_further_x_something", //
			"FROM dummy_entity ", //
			"LEFT OUTER JOIN referenced_entity ref "
				+ "ON ref.dummy_entity = dummy_entity.id1", //
			"LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id", //
			"ORDER BY dummy_entity.x_name DESC", //
			"dummy_entity.x_other ASC");
	}

	@Test // DATAJDBC-101
	public void findAllPagedByUnpaged() {

		String sql = sqlGenerator.getFindAll(Pageable.unpaged());

		assertThat(sql).doesNotContain("ORDER BY").doesNotContain("FETCH FIRST").doesNotContain("OFFSET");
	}

	@Test // DATAJDBC-101
	public void findAllPaged() {

		String sql = sqlGenerator.getFindAll(PageRequest.of(2, 20));

		assertThat(sql).contains("SELECT", //
			"dummy_entity.id1 AS id1", //
			"dummy_entity.x_name AS x_name", //
			"dummy_entity.x_other AS x_other", //
			"ref.x_l1id AS ref_x_l1id", //
			"ref.x_content AS ref_x_content", //
			"ref_further.x_l2id AS ref_further_x_l2id", //
			"ref_further.x_something AS ref_further_x_something", //
			"FROM dummy_entity ", //
			"LEFT OUTER JOIN referenced_entity ref "
				+ "ON ref.dummy_entity = dummy_entity.id1", //
			"LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id", //
			"OFFSET 40", //
			"LIMIT 20");
	}

	@Test // DATAJDBC-101
	public void findAllPagedAndSorted() {

		String sql = sqlGenerator.getFindAll(PageRequest.of(3, 10, Sort.by("name")));

		assertThat(sql).contains("SELECT", //
			"dummy_entity.id1 AS id1", //
			"dummy_entity.x_name AS x_name", //
			"dummy_entity.x_other AS x_other", //
			"ref.x_l1id AS ref_x_l1id", //
			"ref.x_content AS ref_x_content", //
			"ref_further.x_l2id AS ref_further_x_l2id", //
			"ref_further.x_something AS ref_further_x_something", //
			"FROM dummy_entity ", //
			"LEFT OUTER JOIN referenced_entity ref "
				+ "ON ref.dummy_entity = dummy_entity.id1", //
			"LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id", //
			"ORDER BY dummy_entity.x_name ASC", //
			"OFFSET 30", //
			"LIMIT 10");
	}

	@Test // DATAJDBC-1803
	void selectByQueryWithColumnLimit() {

		Query query = Query.empty().columns("id", "alpha", "beta", "gamma");

		String sql = sqlGenerator.selectByQuery(query, new MapSqlParameterSource());

		assertThat(sql).contains( //
			"SELECT dummy_entity.id1 AS id1, dummy_entity.alpha, dummy_entity.beta, dummy_entity.gamma", //
			"FROM dummy_entity" //
		);
	}

	@Test // DATAJDBC-1803
	void selectingSetContentSelectsAllColumns() {

		Query query = Query.empty().columns("elements.content");

		String sql = sqlGenerator.selectByQuery(query, new MapSqlParameterSource());

		assertThat(sql).contains( //
			"SELECT dummy_entity.id1 AS id1, dummy_entity.x_name AS x_name"//
		);
	}

	@Test // DATAJDBC-1803
	void selectByQueryWithMappedColumnPathsRendersCorrectSelection() {

		Query query = Query.empty().columns("ref.content");

		String sql = sqlGenerator.selectByQuery(query, new MapSqlParameterSource());

		assertThat(sql).contains( //
			"SELECT", //
			"ref.x_content AS ref_x_content", //
			"FROM dummy_entity", //
			"LEFT OUTER JOIN referenced_entity ref ON ref.dummy_entity = dummy_entity.id1");
	}

	@Test // DATAJDBC-131, DATAJDBC-111
	public void findAllByProperty() {
		// this would get called when ListParent is the element type of a Set
		String sql = sqlGenerator.getFindAllByProperty(BACKREF, null, false);

		assertThat(sql).contains("SELECT", //
			"dummy_entity.id1 AS id1", //
			"dummy_entity.x_name AS x_name", //
			"dummy_entity.x_other AS x_other", //
			"ref.x_l1id AS ref_x_l1id", //
			"ref.x_content AS ref_x_content", //
			"ref_further.x_l2id AS ref_further_x_l2id", //
			"ref_further.x_something AS ref_further_x_something", //
			"FROM dummy_entity ", //
			"LEFT OUTER JOIN referenced_entity ref "
				+ "ON ref.dummy_entity = dummy_entity.id1", //
			"LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id", //
			"WHERE dummy_entity.backref = :backref");
	}

	@Test // DATAJDBC-223
	public void findAllByPropertyWithMultipartIdentifier() {
		// this would get called when ListParent is the element type of a Set
		Identifier parentIdentifier = Identifier.of(unquoted("backref"), "some-value", String.class) //
			.withPart(unquoted("backref_key"), "key-value", Object.class);
		String sql = sqlGenerator.getFindAllByProperty(parentIdentifier, null, false);

		assertThat(sql).contains("SELECT", //
			"dummy_entity.id1 AS id1", //
			"dummy_entity.x_name AS x_name", //
			"dummy_entity.x_other AS x_other", //
			"ref.x_l1id AS ref_x_l1id", //
			"ref.x_content AS ref_x_content", //
			"ref_further.x_l2id AS ref_further_x_l2id", //
			"ref_further.x_something AS ref_further_x_something", //
			"FROM dummy_entity ", //
			"LEFT OUTER JOIN referenced_entity ref ON ref.dummy_entity = dummy_entity.id1", //
			"LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id", //
			"dummy_entity.backref = :backref", //
			"dummy_entity.backref_key = :backref_key");
	}

	@Test // DATAJDBC-131, DATAJDBC-111
	public void findAllByPropertyWithKey() {
		// this would get called when ListParent is th element type of a Map
		String sql = sqlGenerator.getFindAllByProperty(
			BACKREF, new AggregatePath.ColumnInfo(unquoted("key-column"), unquoted("key-column")), false);

		assertThat(sql).isEqualTo(
			"SELECT dummy_entity.id1 AS id1, dummy_entity.x_name AS x_name, " //
				+ "dummy_entity.x_other AS x_other, " //
				+ "ref.x_l1id AS ref_x_l1id, ref.x_content AS ref_x_content, "
				+ "ref_further.x_l2id AS ref_further_x_l2id, ref_further.x_something "
				+ "AS ref_further_x_something, " //
				+ "dummy_entity.key-column AS key-column " //
				+ "FROM dummy_entity " //
				+ "LEFT OUTER JOIN referenced_entity ref "
				+ "ON ref.dummy_entity = dummy_entity.id1 " //
				+ "LEFT OUTER JOIN second_level_referenced_entity ref_further "
				+ "ON ref_further.referenced_entity = ref.x_l1id " //
				+ "WHERE dummy_entity.backref = :backref");
	}

	@Test // DATAJDBC-130
	public void findAllByPropertyOrderedWithoutKey() {
		assertThatThrownBy(() -> sqlGenerator.getFindAllByProperty(BACKREF, null, true))
			.isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Test // DATAJDBC-131, DATAJDBC-111
	public void findAllByPropertyWithKeyOrdered() {
		// this would get called when ListParent is th element type of a Map
		String sql = sqlGenerator.getFindAllByProperty(BACKREF,
			new AggregatePath.ColumnInfo(unquoted("key-column"), unquoted("key-column")), true);

		assertThat(sql).isEqualTo("SELECT dummy_entity.id1 AS id1, dummy_entity.x_name AS x_name, " //
			+ "dummy_entity.x_other AS x_other, " //
			+ "ref.x_l1id AS ref_x_l1id, ref.x_content AS ref_x_content, "
			+ "ref_further.x_l2id AS ref_further_x_l2id, ref_further.x_something "
			+ "AS ref_further_x_something, " //
			+ "dummy_entity.key-column AS key-column " //
			+ "FROM dummy_entity " //
			+ "LEFT OUTER JOIN referenced_entity ref ON ref.dummy_entity = dummy_entity.id1 " //
			+ "LEFT OUTER JOIN second_level_referenced_entity ref_further "
			+ "ON ref_further.referenced_entity = ref.x_l1id " //
			+ "WHERE dummy_entity.backref = :backref " + "ORDER BY key-column");
	}

	@Test // DATAJDBC-219
	public void updateWithVersion() {

		SqlGenerator sqlGenerator = createSqlGenerator(VersionedEntity.class, AnsiDialect.INSTANCE);

		assertThat(sqlGenerator.getUpdateWithVersion()).containsSubsequence( //
			"UPDATE", //
			"\"VERSIONED_ENTITY\"", //
			"SET", //
			"WHERE", //
			"\"id1\" = :id1", //
			"AND", //
			"\"X_VERSION\" = :___oldOptimisticLockingVersion");
	}

	@Test // DATAJDBC-264
	public void getInsertForEmptyColumnList() {

		SqlGenerator sqlGenerator = createSqlGenerator(IdOnlyEntity.class, JdbcPostgresDialect.INSTANCE);

		String insertSqlStatement = sqlGenerator.getInsert(emptySet());

		assertThat(insertSqlStatement).endsWith(" VALUES (DEFAULT)");
	}

	@Test //DATAJDBC-557
	public void getInsertForEmptyColumnListMsSqlServer() {
		SqlGenerator sqlGenerator = createSqlGenerator(IdOnlyEntity.class, JdbcSqlServerDialect.INSTANCE);

		String insertSqlStatement = sqlGenerator.getInsert(emptySet());

		assertThat(insertSqlStatement).endsWith(" DEFAULT VALUES");
	}

	@Test // GH-821
	public void findAllSortedWithNullHandling_resolvesNullHandlingWhenDialectSupportsIt() {

		SqlGenerator sqlGenerator = createSqlGenerator(DummyEntity.class, JdbcPostgresDialect.INSTANCE);

		String sql = sqlGenerator.getFindAll(
			Sort.by(new Sort.Order(Sort.Direction.ASC, "name", Sort.NullHandling.NULLS_LAST))
		);

		assertThat(sql).contains("ORDER BY \"dummy_entity\".\"x_name\" ASC NULLS LAST");
	}

	@Test // GH-821
	public void findAllSortedWithNullHandling_ignoresNullHandlingWhenDialectDoesNotSupportIt() {

		SqlGenerator sqlGenerator = createSqlGenerator(DummyEntity.class, JdbcSqlServerDialect.INSTANCE);

		String sql = sqlGenerator.getFindAll(
			Sort.by(new Sort.Order(Sort.Direction.ASC, "name", Sort.NullHandling.NULLS_LAST))
		);

		assertThat(sql).endsWith("ORDER BY \"dummy_entity\".\"x_name\" ASC");
	}

	@Test // DATAJDBC-334
	public void getInsertForQuotedColumnName() {

		SqlGenerator sqlGenerator = createSqlGenerator(
			EntityWithQuotedColumnName.class, AnsiDialect.INSTANCE);

		String insert = sqlGenerator.getInsert(emptySet());

		assertThat(insert).isEqualTo("INSERT INTO \"ENTITY_WITH_QUOTED_COLUMN_NAME\" " //
			+ "(\"test\"\"_@123\") " + "VALUES (:test_123)");
	}

	@Test // DATAJDBC-266
	public void joinForOneToOneWithoutIdIncludesTheBackReferenceOfTheOuterJoin() {

		SqlGenerator sqlGenerator = createSqlGenerator(ParentOfNoIdChild.class, AnsiDialect.INSTANCE);

		String findAll = sqlGenerator.getFindAll();

		assertThat(findAll).containsSubsequence("SELECT",
			"\"child\".\"PARENT_OF_NO_ID_CHILD\" AS \"CHILD_PARENT_OF_NO_ID_CHILD\"", "FROM");
	}

	@Test // DATAJDBC-262
	public void update() {

		SqlGenerator sqlGenerator = createSqlGenerator(DummyEntity.class, AnsiDialect.INSTANCE);

		assertThat(sqlGenerator.getUpdate()).containsSubsequence( //
			"UPDATE", //
			"\"DUMMY_ENTITY\"", //
			"SET", //
			"WHERE", //
			"\"id1\" = :id1");
	}

	@Test
	public void getUpsert() {

		SqlGenerator sqlGenerator = createSqlGenerator(DummyEntity.class, AnsiDialect.INSTANCE);

		assertThat(sqlGenerator.getUpsert()).isEqualTo( //
			"INSERT INTO \"DUMMY_ENTITY\" SET \"id1\" = :id1, \"X_NAME\" = :x_name, \"X_OTHER\" = :x_other"
				+ " ON DUPLICATE KEY UPDATE \"X_NAME\" = :x_name, \"X_OTHER\" = :x_other");
	}

	@Test
	public void getUpsertForQuotedColumnName() {

		SqlGenerator sqlGenerator = createSqlGenerator(
			EntityWithQuotedColumnName.class, AnsiDialect.INSTANCE);

		String upsert = sqlGenerator.getUpsert();

		assertThat(upsert).isEqualTo(
			"INSERT INTO \"ENTITY_WITH_QUOTED_COLUMN_NAME\" "
				+ "SET \"test\"\"_@id\" = :test_id, \"test\"\"_@123\" = :test_123"
				+ " ON DUPLICATE KEY UPDATE \"test\"\"_@123\" = :test_123");
	}

	@Test // DATAJDBC-324
	public void readOnlyPropertyExcludedFromQuery_when_generateUpdateSql() {

		final SqlGenerator sqlGenerator = createSqlGenerator(
			EntityWithReadOnlyProperty.class, AnsiDialect.INSTANCE);

		assertThat(sqlGenerator.getUpdate()).isEqualToIgnoringCase( //
			"UPDATE \"ENTITY_WITH_READ_ONLY_PROPERTY\" " //
				+ "SET \"X_NAME\" = :X_NAME " //
				+ "WHERE \"ENTITY_WITH_READ_ONLY_PROPERTY\".\"X_ID\" = :X_ID" //
		);
	}

	@Test // DATAJDBC-334
	public void getUpdateForQuotedColumnName() {

		SqlGenerator sqlGenerator = createSqlGenerator(
			EntityWithQuotedColumnName.class, AnsiDialect.INSTANCE);

		String update = sqlGenerator.getUpdate();

		assertThat(update).isEqualTo("UPDATE \"ENTITY_WITH_QUOTED_COLUMN_NAME\" " //
			+ "SET \"test\"\"_@123\" = :test_123 " //
			+ "WHERE \"ENTITY_WITH_QUOTED_COLUMN_NAME\".\"test\"\"_@id\" = :test_id");
	}

	@Test // DATAJDBC-324
	public void readOnlyPropertyExcludedFromQuery_when_generateInsertSql() {

		final SqlGenerator sqlGenerator = createSqlGenerator(
			EntityWithReadOnlyProperty.class, AnsiDialect.INSTANCE);

		assertThat(sqlGenerator.getInsert(emptySet())).isEqualToIgnoringCase( //
			"INSERT INTO \"ENTITY_WITH_READ_ONLY_PROPERTY\" (\"X_NAME\") " //
				+ "VALUES (:x_name)" //
		);
	}

	@Test // DATAJDBC-324
	public void readOnlyPropertyIncludedIntoQuery_when_generateFindAllSql() {

		final SqlGenerator sqlGenerator = createSqlGenerator(EntityWithReadOnlyProperty.class);

		assertThat(sqlGenerator.getFindAll()).isEqualToIgnoringCase("SELECT "
			+ "entity_with_read_only_property.x_id AS x_id, "
			+ "entity_with_read_only_property.x_name AS x_name, "
			+ "entity_with_read_only_property.x_read_only_value AS x_read_only_value "
			+ "FROM entity_with_read_only_property");
	}

	@Test // DATAJDBC-324
	public void readOnlyPropertyIncludedIntoQuery_when_generateFindAllByPropertySql() {

		final SqlGenerator sqlGenerator = createSqlGenerator(EntityWithReadOnlyProperty.class);

		assertThat(sqlGenerator.getFindAllByProperty(
			BACKREF, new AggregatePath.ColumnInfo(unquoted("key-column"), unquoted("key-column")),
			true)).isEqualToIgnoringCase( //
			"SELECT " //
				+ "entity_with_read_only_property.x_id AS x_id, " //
				+ "entity_with_read_only_property.x_name AS x_name, " //
				+ "entity_with_read_only_property.x_read_only_value AS x_read_only_value, " //
				+ "entity_with_read_only_property.key-column AS key-column " //
				+ "FROM entity_with_read_only_property " //
				+ "WHERE entity_with_read_only_property.backref = :backref " //
				+ "ORDER BY key-column" //
		);
	}

	@Test // DATAJDBC-324
	public void readOnlyPropertyIncludedIntoQuery_when_generateFindAllInListSql() {

		final SqlGenerator sqlGenerator = createSqlGenerator(EntityWithReadOnlyProperty.class);

		assertThat(sqlGenerator.getFindAllInList()).isEqualToIgnoringCase( //
			"SELECT " //
				+ "entity_with_read_only_property.x_id AS x_id, " //
				+ "entity_with_read_only_property.x_name AS x_name, " //
				+ "entity_with_read_only_property.x_read_only_value AS x_read_only_value " //
				+ "FROM entity_with_read_only_property " //
				+ "WHERE entity_with_read_only_property.x_id IN (:ids)" //
		);
	}

	@Test // DATAJDBC-324
	public void readOnlyPropertyIncludedIntoQuery_when_generateFindOneSql() {

		final SqlGenerator sqlGenerator = createSqlGenerator(EntityWithReadOnlyProperty.class);

		assertThat(sqlGenerator.getFindOne()).isEqualToIgnoringCase( //
			"SELECT " //
				+ "entity_with_read_only_property.x_id AS x_id, " //
				+ "entity_with_read_only_property.x_name AS x_name, " //
				+ "entity_with_read_only_property.x_read_only_value AS x_read_only_value " //
				+ "FROM entity_with_read_only_property " //
				+ "WHERE entity_with_read_only_property.x_id = :x_id" //
		);
	}

	@Test // DATAJDBC-340
	public void deletingLongChain() {

		assertThat(
			createSqlGenerator(Chain4.class).createDeleteByPath(
				getPath("chain3.chain2.chain1.chain0", Chain4.class))) //
			.isEqualTo("DELETE FROM chain0 " //
				+ "WHERE chain0.chain1 IN ("
				+ "SELECT chain1.x_one " //
				+ "FROM chain1 " //
				+ "WHERE chain1.chain2 IN (" //
				+ "SELECT chain2.x_two " //
				+ "FROM chain2 " //
				+ "WHERE chain2.chain3 IN (" //
				+ "SELECT chain3.x_three " //
				+ "FROM chain3 " //
				+ "WHERE chain3.chain4 = :x_four" //
				+ ")))");
	}

	@Test // DATAJDBC-359
	public void deletingLongChainNoId() {

		assertThat(createSqlGenerator(NoIdChain4.class)
			.createDeleteByPath(getPath("chain3.chain2.chain1.chain0", NoIdChain4.class))) //
			.isEqualTo("DELETE FROM no_id_chain0 WHERE no_id_chain0.no_id_chain4 = :x_four");
	}

	@Test // DATAJDBC-359
	public void deletingLongChainNoIdWithBackreferenceNotReferencingTheRoot() {

		assertThat(createSqlGenerator(IdIdNoIdChain.class)
			.createDeleteByPath(
				getPath("idNoIdChain.chain4.chain3.chain2.chain1.chain0", IdIdNoIdChain.class))) //
			.isEqualTo( //
				"DELETE FROM no_id_chain0 " //
					+ "WHERE no_id_chain0.no_id_chain4 IN (" //
					+ "SELECT no_id_chain4.x_four " //
					+ "FROM no_id_chain4 " //
					+ "WHERE no_id_chain4.id_no_id_chain IN (" //
					+ "SELECT id_no_id_chain.x_id " //
					+ "FROM id_no_id_chain " //
					+ "WHERE id_no_id_chain.id_id_no_id_chain = :x_id" //
					+ "))");
	}

	@Test // DATAJDBC-340
	public void noJoinForSimpleColumn() {
		assertThat(generateJoin("id", DummyEntity.class)).isNull();
	}

	@Test // DATAJDBC-340
	public void joinForSimpleReference() {

		SqlGenerator.Join join = generateJoin("ref", DummyEntity.class);

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(join.joinTable().getName()).isEqualTo(SqlIdentifier.quoted("REFERENCED_ENTITY"));
			softly.assertThat(join.condition()).isEqualTo(equalsCondition(SqlIdentifier.quoted("DUMMY_ENTITY"),
				SqlIdentifier.quoted("id1"), join.joinTable(), SqlIdentifier.quoted("DUMMY_ENTITY")));
		});
	}

	@Test // DATAJDBC-340
	public void noJoinForCollectionReference() {

		SqlGenerator.Join join = generateJoin("elements", DummyEntity.class);

		assertThat(join).isNull();

	}

	@Test // DATAJDBC-340
	public void noJoinForMappedReference() {

		SqlGenerator.Join join = generateJoin("mappedElements", DummyEntity.class);

		assertThat(join).isNull();
	}

	@Test // DATAJDBC-340
	public void joinForSecondLevelReference() {

		SqlGenerator.Join join = generateJoin("ref.further", DummyEntity.class);

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(join.joinTable().getName())
				.isEqualTo(SqlIdentifier.quoted("SECOND_LEVEL_REFERENCED_ENTITY"));
			softly.assertThat(join.condition())
				.isEqualTo(equalsCondition(
					org.springframework.data.relational.core.sql.Table.create("REFERENCED_ENTITY")
						.as(SqlIdentifier.quoted("ref")),
					SqlIdentifier.quoted("X_L1ID"), join.joinTable(), SqlIdentifier.quoted("REFERENCED_ENTITY")));
		});
	}

	@Test // DATAJDBC-340
	public void joinForOneToOneWithoutId() {

		SqlGenerator.Join join = generateJoin("child", ParentOfNoIdChild.class);
		TableLike joinTable = join.joinTable();

		SoftAssertions.assertSoftly(softly -> {

			softly.assertThat(joinTable.getName()).isEqualTo(SqlIdentifier.quoted("NO_ID_CHILD"));
			softly.assertThat(joinTable).isInstanceOf(Aliased.class);
			softly.assertThat(((Aliased)joinTable).getAlias()).isEqualTo(SqlIdentifier.quoted("child"));
			softly.assertThat(join.condition()).isEqualTo(equalsCondition(SqlIdentifier.quoted("PARENT_OF_NO_ID_CHILD"),
				SqlIdentifier.quoted("X_ID"), join.joinTable(), SqlIdentifier.quoted("PARENT_OF_NO_ID_CHILD")));

		});
	}

	private SqlGenerator.Join generateJoin(String path, Class<?> type) {
		return createSqlGenerator(type, AnsiDialect.INSTANCE)
			.getJoin(context.getAggregatePath(PropertyPathTestingUtils.toPath(path, type, context)));
	}

	@Test // DATAJDBC-340
	public void simpleColumn() {

		assertThat(generatedColumn("id", DummyEntity.class)) //
			.extracting(
				c -> c.getName(),
				c -> c.getTable().getName(),
				c -> getAlias(c.getTable()),
				this::getAlias)
			.containsExactly(quoted("id1"), quoted("DUMMY_ENTITY"), null,
				quoted("id1"));
	}

	@Test // DATAJDBC-340
	public void columnForIndirectProperty() {

		assertThat(generatedColumn("ref.l1id", DummyEntity.class)) //
			.extracting(
				c -> c.getName(),
				c -> c.getTable().getName(),
				c -> getAlias(c.getTable()),
				this::getAlias) //
			.containsExactly(quoted("X_L1ID"), quoted("REFERENCED_ENTITY"),
				quoted("ref"), quoted("REF_X_L1ID"));
	}

	@Test // DATAJDBC-340
	public void noColumnForReferencedEntity() {

		assertThat(generatedColumn("ref", DummyEntity.class)).isNull();
	}

	@Test // DATAJDBC-340
	public void columnForReferencedEntityWithoutId() {

		assertThat(generatedColumn("child", ParentOfNoIdChild.class)) //
			.extracting(
				c -> c.getName(),
				c -> c.getTable().getName(),
				c -> getAlias(c.getTable()),
				this::getAlias) //
			.containsExactly(quoted("PARENT_OF_NO_ID_CHILD"), quoted("NO_ID_CHILD"),
				quoted("child"), quoted("CHILD_PARENT_OF_NO_ID_CHILD"));
	}

	@Test
	void updateWithAlias() {

		assertThat(createSqlGenerator(DummyAliasEntity.class, NonQuotingDialect.INSTANCE).getUpdate())
			.contains("WHERE dummy_entity.id1 = :id1");
	}

	@Test
	void deleteWithAlias() {
		assertThat(createSqlGenerator(DummyAliasEntity.class, NonQuotingDialect.INSTANCE).getDeleteById())
			.contains("WHERE dummy_entity.id1 = :id");
	}

	@Test
	void updateVersionWithAlias() {
		assertThat(createSqlGenerator(VersionedAliasEntity.class, NonQuotingDialect.INSTANCE).getUpdateWithVersion())
			.contains("WHERE versioned_entity.id1 = :id1 AND versioned_entity.x_version = :___old");
	}

	@Test
	void deleteVersionWithAlias() {
		assertThat(createSqlGenerator(VersionedAliasEntity.class, NonQuotingDialect.INSTANCE).getDeleteByIdAndVersion())
			.contains("versioned_entity.id1 = :id1 AND versioned_entity.x_version = :___old");
	}

	@Test
	void softDeleteByIdWithBoolean() {
		assertThat(
			createSqlGenerator(BooleanValueSoftDeleteArticle.class, NonQuotingDialect.INSTANCE).getSoftDeleteById()
		).isEqualTo(
			"UPDATE boolean_value_article SET x_deleted = :x_deleted WHERE boolean_value_article.x_id = :x_id"
		);
	}

	@Test
	void softDeleteByIdWithPlainBoolean() {
		assertThat(createSqlGenerator(SoftDeleteArticle.class, NonQuotingDialect.INSTANCE).getSoftDeleteById())
			.isEqualTo("UPDATE article SET x_deleted = :x_deleted WHERE article.x_id = :x_id");
	}

	@Test
	void softDeleteByIdWithString() {
		assertThat(
			createSqlGenerator(StringValueSoftDeleteArticle.class, NonQuotingDialect.INSTANCE).getSoftDeleteById()
		).isEqualTo(
			"UPDATE string_value_article SET x_state = :x_state WHERE string_value_article.x_id = :x_id"
		);
	}

	@Test
	void softDeleteByIdInWithBoolean() {
		assertThat(
			createSqlGenerator(BooleanValueSoftDeleteArticle.class, NonQuotingDialect.INSTANCE).getSoftDeleteByIdIn()
		).isEqualTo(
			"UPDATE boolean_value_article SET x_deleted = :x_deleted WHERE boolean_value_article.x_id IN (:ids)"
		);
	}

	@Test
	void softDeleteByIdInWithString() {
		assertThat(
			createSqlGenerator(StringValueSoftDeleteArticle.class, NonQuotingDialect.INSTANCE).getSoftDeleteByIdIn()
		).isEqualTo(
			"UPDATE string_value_article SET x_state = :x_state WHERE string_value_article.x_id IN (:ids)"
		);
	}

	@Test
	public void softDeleteAll() {

		String sql = createSqlGenerator(SoftDeleteArticle.class, NonQuotingDialect.INSTANCE)
			.createSoftDeleteAllSql(null);

		assertThat(sql).isEqualTo("UPDATE article SET x_deleted = :x_deleted");
	}

	@Test
	public void cascadingSoftDeleteAllFirstLevel() {

		String sql = createSqlGenerator(SoftDeleteArticle.class, NonQuotingDialect.INSTANCE)
			.createSoftDeleteAllSql(getPath("ref", SoftDeleteArticle.class));

		assertThat(sql).isEqualTo(
			"UPDATE referenced_article "
				+ "SET x_reference_deleted = :x_reference_deleted "
				+ "WHERE referenced_article.article IS NOT NULL");
	}

	@Test
	public void cascadingSoftDeleteAllSecondLevel() {

		String sql = createSqlGenerator(SoftDeleteArticle.class, NonQuotingDialect.INSTANCE)
			.createSoftDeleteAllSql(getPath("ref.further", SoftDeleteArticle.class));

		assertThat(sql).isEqualTo(
			"UPDATE second_referenced_article "
				+ "SET x_second_reference_deleted = :x_second_reference_deleted "
				+ "WHERE second_referenced_article.referenced_article IN ("
				+ "SELECT referenced_article.x_id FROM referenced_article "
				+ "WHERE referenced_article.article IS NOT NULL)");
	}

	@Test
	public void softDeleteAllMap() {

		String sql = createSqlGenerator(SoftDeleteArticle.class, NonQuotingDialect.INSTANCE)
			.createSoftDeleteAllSql(getPath("mappedElements", SoftDeleteArticle.class));

		assertThat(sql).isEqualTo("UPDATE soft_delete_element "
			+ "SET x_element_deleted = :x_element_deleted "
			+ "WHERE soft_delete_element.article IS NOT NULL");
	}

	@Test
	void softDeleteByIdAndVersionWithBoolean() {
		assertThat(
			createSqlGenerator(
				BooleanValueSoftDeleteArticle.class,
				NonQuotingDialect.INSTANCE
			).getSoftDeleteByIdAndVersion()
		).isEqualTo(
			"UPDATE boolean_value_article "
				+ "SET x_deleted = :x_deleted, "
				+ "x_version = :x_version "
				+ "WHERE boolean_value_article.x_id = :x_id "
				+ "AND boolean_value_article.x_version = :___oldOptimisticLockingVersion"
		);
	}

	@Test
	void softDeleteByIdAndVersionWithString() {
		assertThat(
			createSqlGenerator(
				StringValueSoftDeleteArticle.class,
				NonQuotingDialect.INSTANCE
			).getSoftDeleteByIdAndVersion()
		).isEqualTo(
			"UPDATE string_value_article "
				+ "SET x_state = :x_state, "
				+ "x_version = :x_version "
				+ "WHERE string_value_article.x_id = :x_id "
				+ "AND string_value_article.x_version = :___oldOptimisticLockingVersion"
		);
	}

	private SqlIdentifier getAlias(Object maybeAliased) {

		if (maybeAliased instanceof Aliased) {
			return ((Aliased)maybeAliased).getAlias();
		}
		return null;
	}

	private org.springframework.data.relational.core.sql.Column generatedColumn(
		String path, Class<?> type) {

		return createSqlGenerator(type, AnsiDialect.INSTANCE)
			.getColumn(context.getAggregatePath(PropertyPathTestingUtils.toPath(path, type, context)));
	}

	private PersistentPropertyPath<RelationalPersistentProperty> getPath(
		String path, Class<?> baseType) {
		return PersistentPropertyPathTestUtils.getPath(context, path, baseType);
	}

	@SuppressWarnings("unused")
	static class DummyEntity {

		@Column("id1")
		@Id
		Long id;
		String name;
		ReferencedEntity ref;
		Set<Element> elements;
		Map<Integer, Element> mappedElements;
		AggregateReference<OtherAggregate, Long> other;
	}

	@SuppressWarnings("unused")
	@Table("dummy_entity")
	@SqlTableAlias("n_dummy")
	static class DummyAliasEntity {

		@Column("id1")
		@Id
		Long id;
		String name;
		ReferencedEntity ref;
		Set<Element> elements;
		Map<Integer, Element> mappedElements;
		AggregateReference<OtherAggregate, Long> other;
	}

	@SuppressWarnings("unused")
	@Table("versioned_entity")
	@SqlTableAlias("n_dummy")
	static class VersionedAliasEntity extends DummyAliasEntity {

		@Version
		Integer version;
	}

	static class VersionedEntity extends DummyEntity {
		@Version
		Integer version;
	}

	@SuppressWarnings("unused")
	static class ReferencedEntity {

		@Id
		Long l1id;
		String content;
		SecondLevelReferencedEntity further;
	}

	@SuppressWarnings("unused")
	static class SecondLevelReferencedEntity {

		@Id
		Long l2id;
		String something;
	}

	static class Element {
		@Id
		Long id;
		String content;
	}

	@SuppressWarnings("unused")
	static class ParentOfNoIdChild {
		@Id
		Long id;
		NoIdChild child;
	}

	static class NoIdChild {
	}

	static class OtherAggregate {
		@Id
		Long id;
		String name;
	}

	private static class PrefixingNamingStrategy extends DefaultNamingStrategy {

		@Override
		public String getColumnName(RelationalPersistentProperty property) {
			return "x_" + super.getColumnName(property);
		}

	}

	@SuppressWarnings("unused")
	static class IdOnlyEntity {

		@Id
		Long id;
	}

	@SuppressWarnings("unused")
	static class EntityWithReadOnlyProperty {
		@Id
		Long id;
		String name;
		@ReadOnlyProperty
		String readOnlyValue;
	}

	static class EntityWithQuotedColumnName {
		// these column names behave like single double quote in the name
		// since the get quoted and then doubling the double
		// quote escapes it.
		@Id
		@Column("test\"\"_@id")
		Long id;
		@Column("test\"\"_@123")
		String name;
	}

	@SuppressWarnings("unused")
	static class Chain0 {
		@Id
		Long zero;
		String zeroValue;
	}

	@SuppressWarnings("unused")
	static class Chain1 {
		@Id
		Long one;
		String oneValue;
		Chain0 chain0;
	}

	@SuppressWarnings("unused")
	static class Chain2 {
		@Id
		Long two;
		String twoValue;
		Chain1 chain1;
	}

	@SuppressWarnings("unused")
	static class Chain3 {
		@Id
		Long three;
		String threeValue;
		Chain2 chain2;
	}

	@SuppressWarnings("unused")
	static class Chain4 {
		@Id
		Long four;
		String fourValue;
		Chain3 chain3;
	}

	static class NoIdChain0 {
		String zeroValue;
	}

	static class NoIdChain1 {
		String oneValue;
		NoIdChain0 chain0;
	}

	static class NoIdChain2 {
		String twoValue;
		NoIdChain1 chain1;
	}

	static class NoIdChain3 {
		String threeValue;
		NoIdChain2 chain2;
	}

	static class NoIdChain4 {
		@Id
		Long four;
		String fourValue;
		NoIdChain3 chain3;
	}

	static class IdNoIdChain {
		@Id
		Long id;
		NoIdChain4 chain4;
	}

	static class IdIdNoIdChain {
		@Id
		Long id;
		IdNoIdChain idNoIdChain;
	}

	@Table("boolean_value_article")
	static class BooleanValueSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn.Boolean(valueAsDeleted = "true")
		boolean deleted;

		@Version
		Integer version;
	}

	@Table("string_value_article")
	static class StringValueSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn.String(valueAsDeleted = "DELETE")
		ArticleState state;

		@Version
		Integer version;

		enum ArticleState {
			OPEN, CLOSE, DELETE
		}
	}

	@Table("article")
	static class SoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean deleted;

		@Version
		Integer version;

		ReferencedSoftDeleteArticle ref;

		Map<Integer, SoftDeleteElement> mappedElements;
	}

	@Table("referenced_article")
	static class ReferencedSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean reference_deleted; // @checkstyle:ignore

		SecondReferencedSoftDeleteArticle further;
	}

	@Table("second_referenced_article")
	static class SecondReferencedSoftDeleteArticle {

		@Id
		Long id;

		String contents;

		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean second_reference_deleted; // @checkstyle:ignore
	}

	@Table("soft_delete_element")
	static class SoftDeleteElement {
		@Id
		Long id;
		@SoftDeleteColumn(type = ValueType.BOOLEAN, valueAsDeleted = "true")
		boolean element_deleted; // @checkstyle:ignore
	}
}
