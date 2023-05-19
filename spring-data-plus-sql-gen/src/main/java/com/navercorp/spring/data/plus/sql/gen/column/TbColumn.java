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

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

/**
 * The type Tb column.
 *
 * @author Myeonghyeon Lee
 */
public final class TbColumn {
	private final PersistentPropertyPathExtension pathExtension;
	private final IdentifierProcessing identifierProcessing;

	private final String path;
	private final String column;
	private final String alias;

	/**
	 * Instantiates a new Tb column.
	 *
	 * @param pathExtension        the path extension
	 * @param identifierProcessing the identifier processing
	 */
	TbColumn(PersistentPropertyPathExtension pathExtension, IdentifierProcessing identifierProcessing) {
		this.pathExtension = pathExtension;
		this.identifierProcessing = identifierProcessing;

		this.path = pathExtension.getRequiredPersistentPropertyPath().toDotPath();
		this.column = pathExtension.getColumnName().getReference();
		String aliasValue = this.column;
		if (pathExtension.getColumnAlias() != null) {
			aliasValue = pathExtension.getColumnAlias().getReference();
		}
		this.alias = aliasValue;
	}

	/**
	 * Create tb column.
	 *
	 * @param pathExtension        the path extension
	 * @param identifierProcessing the identifier processing
	 * @return the tb column
	 */
	public static TbColumn create(
		PersistentPropertyPathExtension pathExtension,
		IdentifierProcessing identifierProcessing) {

		return new TbColumn(pathExtension, identifierProcessing);
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
	 * Column string.
	 *
	 * @return the string
	 */
	public String column() {
		return this.column;
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
