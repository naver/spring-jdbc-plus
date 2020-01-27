package com.navercorp.spring.data.plus.sql.gen.dsl;

import com.navercorp.spring.data.plus.sql.gen.column.TbColumn;

public class SqlDsl {
	public static String param(String parameterName) {
		return ":" + parameterName;
	}

	public static String param(TbColumn column) {
		return param(column.path());
	}
}
