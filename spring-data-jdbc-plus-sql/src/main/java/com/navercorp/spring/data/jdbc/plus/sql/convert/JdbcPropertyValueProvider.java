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

import org.jspecify.annotations.Nullable;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.domain.RowDocument;

import com.navercorp.spring.data.jdbc.plus.support.convert.PropertyPathUtils;

/**
 * {@link PropertyValueProvider} obtaining values from a RowDocument.
 *
 * @author Jens Schauder
 * @author Myeonghyeon Lee
 * @since 2.0
 *
 * Copy org.springframework.data.jdbc.core.convert.JdbcPropertyValueProvider
 * Verified: c0803ddafef7a4bc4ec070df6581d46c4d59ff4a
 */
class JdbcPropertyValueProvider implements PropertyValueProvider<RelationalPersistentProperty> {
	private final AggregatePath basePath;
	private final RowDocument rowDocument;

	/**
	 * @param basePath path from the aggregate root relative to which all properties get resolved.
	 * @param rowDocument the RowDocument from which to obtain the actual values.
	 */
	JdbcPropertyValueProvider(AggregatePath basePath, RowDocument rowDocument) {

		this.rowDocument = rowDocument;
		this.basePath = basePath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable T getPropertyValue(RelationalPersistentProperty property) {
		return (T)rowDocument.get(getColumnName(property));
	}

	/**
	 * Returns whether the underlying source contains a data source
	 * for the given {@link RelationalPersistentProperty}.
	 *
	 * @param property
	 * @return
	 */
	public boolean hasProperty(RelationalPersistentProperty property) {
		return rowDocument.containsKey(getColumnName(property));
	}

	private String getColumnName(RelationalPersistentProperty property) {
		AggregatePath path = basePath.append(property);
		return PropertyPathUtils.getColumnAlias(path).getReference();
	}

	public JdbcPropertyValueProvider extendBy(RelationalPersistentProperty property) {
		return new JdbcPropertyValueProvider(basePath.append(property), rowDocument);
	}
}
