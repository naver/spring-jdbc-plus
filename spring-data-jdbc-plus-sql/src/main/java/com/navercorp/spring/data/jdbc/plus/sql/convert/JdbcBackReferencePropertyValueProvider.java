/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

/**
 * {@link PropertyValueProvider} obtaining values from a ResultSetAccessor. For a given id property it provides
 * the value in the resultset under which other entities refer back to it.
 *
 * @author Jens Schauder
 * @author Myeonghyeon Lee
 * @since 2.0
 *
 * Copy org.springframework.data.jdbc.core.convert.JdbcBackReferencePropertyValueProvider
 * Verified: c0803ddafef7a4bc4ec070df6581d46c4d59ff4a
 */
class JdbcBackReferencePropertyValueProvider implements PropertyValueProvider<RelationalPersistentProperty> {
	private final AggregatePath basePath;
	private final ResultSetAccessor resultSet;

	/**
	 * @param basePath path from the aggregate root relative to which all properties get resolved.
	 * @param resultSet the ResultSetAccessor from which to obtain the actual values.
	 */
	JdbcBackReferencePropertyValueProvider(AggregatePath basePath, ResultSetAccessor resultSet) {

		this.resultSet = resultSet;
		this.basePath = basePath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPropertyValue(RelationalPersistentProperty property) {
		return (T)resultSet.getObject(PropertyPathUtils.getReverseColumnAlias(basePath.append(property))
			.getReference());
	}

	public JdbcBackReferencePropertyValueProvider extendBy(RelationalPersistentProperty property) {
		return new JdbcBackReferencePropertyValueProvider(basePath.append(property), resultSet);
	}
}
