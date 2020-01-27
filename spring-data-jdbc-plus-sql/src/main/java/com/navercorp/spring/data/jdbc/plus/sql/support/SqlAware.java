package com.navercorp.spring.data.jdbc.plus.sql.support;

import com.navercorp.spring.data.jdbc.plus.sql.convert.SqlProvider;

public interface SqlAware {
	void setSql(SqlProvider sql);
}
