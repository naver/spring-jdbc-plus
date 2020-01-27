package com.navercorp.spring.data.jdbc.plus.sql.support;

import com.navercorp.spring.data.jdbc.plus.sql.convert.SqlProvider;

public abstract class SqlGeneratorSupport implements SqlAware {
	protected SqlProvider sql;

	public void setSql(SqlProvider sql) {
		this.sql = sql;
	}
}
