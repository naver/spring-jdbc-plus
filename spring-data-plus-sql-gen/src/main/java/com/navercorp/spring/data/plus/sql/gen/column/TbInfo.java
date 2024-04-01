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

package com.navercorp.spring.data.plus.sql.gen.column;

import org.springframework.data.relational.core.mapping.AggregatePath;

/**
 * The type Tb info.
 *
 * @author Myeonghyeon Lee
 */
public final class TbInfo {
	private final String path;
	private final String table;
	private final String alias;

	/**
	 * Instantiates a new Tb info.
	 *
	 * @param aggregatePath        the path extension
	 */
	TbInfo(AggregatePath aggregatePath) {
		this.path = aggregatePath.getRequiredPersistentPropertyPath().toDotPath();
		this.table = aggregatePath.getTableInfo().qualifiedTableName().getReference();
		String aliasValue = this.table;
		if (aggregatePath.getTableInfo().tableAlias() != null) {
			aliasValue = aggregatePath.getTableInfo().tableAlias().getReference();
		}
		this.alias = aliasValue;
	}

	/**
	 * Create tb info.
	 *
	 * @param aggregatePath        the aggregate path
	 * @return the tb info
	 */
	public static TbInfo create(AggregatePath aggregatePath) {

		return new TbInfo(aggregatePath);
	}

	/**
	 * Path string.
	 *
	 * @return the string
	 */
	public String path() {
		return this.path;
	}

	/**
	 * Table string.
	 *
	 * @return the string
	 */
	public String table() {
		return this.table;
	}

	/**
	 * Alias string.
	 *
	 * @return the string
	 */
	public String alias() {
		return this.alias;
	}
}
