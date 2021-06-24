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

package com.navercorp.spring.data.jdbc.plus.sql.parametersource;

import java.util.Map;

import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * The type Default sql parameter source factory.
 *
 * @author Myeonghyeon Lee
 */
public class DefaultSqlParameterSourceFactory implements SqlParameterSourceFactory {
	private final RelationalMappingContext mappingContext;
	private final IdentifierProcessing identifierProcessing;
	private final EntitySqlParameterSourceApplier parameterSourceApplier;

	/**
	 * Instantiates a new Default sql parameter source factory.
	 *
	 * @param mappingContext       the mapping context
	 * @param jdbcConverter        the jdbc converter
	 * @param identifierProcessing the identifier processing
	 */
	public DefaultSqlParameterSourceFactory(
		RelationalMappingContext mappingContext,
		JdbcConverter jdbcConverter,
		IdentifierProcessing identifierProcessing) {

		this.mappingContext = mappingContext;
		this.identifierProcessing = identifierProcessing;
		this.parameterSourceApplier = new EntitySqlParameterSourceApplier(mappingContext, jdbcConverter);
	}

	@Override
	public BeanPropertySqlParameterSource beanParameterSource(Object bean) {
		return new BeanPropertySqlParameterSource(bean);
	}

	@Override
	public MapSqlParameterSource mapParameterSource(Map<String, ?> map) {
		return new MapSqlParameterSource(map);
	}

	@Override
	public SqlParameterSource entityParameterSource(Object entity) {
		SqlIdentifierParameterSource parameterSource =
			new SqlIdentifierParameterSource(this.identifierProcessing);
		RelationalPersistentEntity<?> persistentEntity =
			this.mappingContext.getRequiredPersistentEntity(entity.getClass());
		this.parameterSourceApplier.addParameterSource(parameterSource, entity, persistentEntity, "");
		return parameterSource;
	}
}
