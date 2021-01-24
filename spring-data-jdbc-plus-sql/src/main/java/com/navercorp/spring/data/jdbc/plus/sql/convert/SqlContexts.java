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

package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Table;

/**
 * Interface for internal extension.
 *
 * @author Myeonghyeon Lee
 */
public interface SqlContexts {
	/**
	 * Gets id column.
	 *
	 * @return the id column
	 */
	Column getIdColumn();

	/**
	 * Gets table.
	 *
	 * @return the table
	 */
	Table getTable();

	/**
	 * Gets table.
	 *
	 * @param path the path
	 * @return the table
	 */
	Table getTable(PersistentPropertyPathExtension path);

	/**
	 * Gets column.
	 *
	 * @param path the path
	 * @return the column
	 */
	Column getColumn(PersistentPropertyPathExtension path);

	/**
	 * Gets version column.
	 *
	 * @return the version column
	 */
	Column getVersionColumn();

	/**
	 * Gets reverse column.
	 *
	 * @param path the path
	 * @return the reverse column
	 */
	Column getReverseColumn(PersistentPropertyPathExtension path);
}
