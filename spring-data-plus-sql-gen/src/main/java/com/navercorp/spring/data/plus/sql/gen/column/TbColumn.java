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
import org.springframework.data.relational.core.sql.IdentifierProcessing;

/**
 * The type Tb column.
 *
 * @author Myeonghyeon Lee
 */
public final class TbColumn {
	private final AggregatePath pathExtension;
	private final IdentifierProcessing identifierProcessing;

	private final String path;
	private final String column;
	private final String alias;

	/**
	 * Instantiates a new Tb column.
	 *
	 * @param aggregatePath        the aggregate path
	 * @param identifierProcessing the identifier processing
	 */
	TbColumn(AggregatePath aggregatePath, IdentifierProcessing identifierProcessing) {
		this.pathExtension = aggregatePath;
		this.identifierProcessing = identifierProcessing;

		this.path = aggregatePath.getRequiredPersistentPropertyPath().toDotPath();
		this.column = aggregatePath.getColumnInfo().name().getReference();
		this.alias = aggregatePath.getColumnInfo().alias().getReference();
	}

	/**
	 * Create tb column.
	 *
	 * @param pathExtension        the path extension
	 * @param identifierProcessing the identifier processing
	 * @return the tb column
	 */
	public static TbColumn create(
		AggregatePath pathExtension,
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
