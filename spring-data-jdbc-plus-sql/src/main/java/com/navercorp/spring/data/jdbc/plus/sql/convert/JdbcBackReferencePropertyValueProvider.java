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
import org.springframework.data.relational.core.mapping.PersistentPropertyPathExtension;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.IdentifierProcessing;

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

	private final IdentifierProcessing identifierProcessing;
	private final PersistentPropertyPathExtension basePath;
	private final ResultSetAccessor resultSet;

	/**
	 * @param identifierProcessing used for converting the
	 *          {@link org.springframework.data.relational.core.sql.SqlIdentifier} from a property to a column label
	 * @param basePath path from the aggregate root relative to which all properties get resolved.
	 * @param resultSet the ResultSetAccessor from which to obtain the actual values.
	 */
	JdbcBackReferencePropertyValueProvider(IdentifierProcessing identifierProcessing,
		PersistentPropertyPathExtension basePath, ResultSetAccessor resultSet) {

		this.resultSet = resultSet;
		this.basePath = basePath;
		this.identifierProcessing = identifierProcessing;
	}

	@Override
	public <T> T getPropertyValue(RelationalPersistentProperty property) {
		PersistentPropertyPathExtension path = basePath.extendBy(property);

		return (T)resultSet.getObject(PropertyPathUtils.getReverseColumnAlias(path)
			.getReference());
	}

	public JdbcBackReferencePropertyValueProvider extendBy(RelationalPersistentProperty property) {
		return new JdbcBackReferencePropertyValueProvider(
			identifierProcessing, basePath.extendBy(property), resultSet);
	}
}
