package com.navercorp.spring.data.jdbc.plus.sql.convert;

public class PlusSqlGeneratorSource {
	private final SqlGeneratorSource sqlGeneratorSource;

	public PlusSqlGeneratorSource(SqlGeneratorSource sqlGeneratorSource) {
		this.sqlGeneratorSource = sqlGeneratorSource;
	}

	public String deleteById(Class<?> domainType) {
		return sqlGeneratorSource.getSqlGenerator(domainType).getDeleteById();
	}
}
