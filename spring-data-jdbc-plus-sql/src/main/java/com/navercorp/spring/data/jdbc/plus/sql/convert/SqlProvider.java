package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

public class SqlProvider {
	private final SqlGeneratorSource generatorSource;
	private final Map<Class<?>, String> selectColumnCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> fromTableCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> selectAggregateColumnCache = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> fromAggregateTableCache = new ConcurrentHashMap<>();

	public SqlProvider(RelationalMappingContext context, JdbcConverter converter, Dialect dialect) {
		this.generatorSource = new SqlGeneratorSource(context, converter, dialect);
	}

	public SqlProvider(SqlGeneratorSource generatorSource) {
		this.generatorSource = generatorSource;
	}

	public String columns(Class<?> entityType) {
		return this.selectColumnCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectFrom = generator.selectFrom();
			int fromIndex = selectFrom.lastIndexOf(" FROM ");
			if (fromIndex < 0) {
				return selectFrom.substring(6) + " ";
			}
			return selectFrom.substring(6, fromIndex) + " ";
		});
	}

	public String tables(Class<?> entityType) {
		return this.fromTableCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectFrom = generator.selectFrom();
			int fromIndex = selectFrom.lastIndexOf(" FROM ");
			if (fromIndex < 0) {
				throw new MappingException(
					"Can not generate tables clause. mapping id does not exist. entity: " + entityType);
			}
			return " " + selectFrom.substring(fromIndex + 6) + " ";
		});
	}

	public String aggregateColumns(Class<?> entityType) {
		return this.selectAggregateColumnCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectAggregateFrom = generator.selectAggregateFrom();
			int fromIndex = selectAggregateFrom.lastIndexOf(" FROM ");
			return fromIndex > 0
				? selectAggregateFrom.substring(6, fromIndex) + " "
				: selectAggregateFrom + " ";
		});
	}

	public String aggregateTables(Class<?> entityType) {
		return this.fromAggregateTableCache.computeIfAbsent(entityType, e -> {
			SqlGenerator generator = this.generatorSource.getSqlGenerator(entityType);
			String selectAggregateFrom = generator.selectAggregateFrom();
			int fromIndex = selectAggregateFrom.lastIndexOf(" FROM ");
			if (fromIndex < 0) {
				throw new MappingException(
					"Can not generate aggregateTables clause. mapping id does not exist. entity: " + entityType);
			}
			return " " + selectAggregateFrom.substring(fromIndex + 6) + " ";
		});
	}

	public SqlGeneratorSource getSqlGeneratorSource() {
		return this.generatorSource;
	}
}
