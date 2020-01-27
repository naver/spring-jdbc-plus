/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
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

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * The interface Sql parameter source factory.
 *
 * @author Myeonghyeon Lee
 */
public interface SqlParameterSourceFactory {
	/**
	 * Bean parameter source bean property sql parameter source.
	 *
	 * @param bean the bean
	 * @return the bean property sql parameter source
	 */
	BeanPropertySqlParameterSource beanParameterSource(Object bean);

	/**
	 * Map parameter source map sql parameter source.
	 *
	 * @param map the map
	 * @return the map sql parameter source
	 */
	MapSqlParameterSource mapParameterSource(Map<String, ?> map);

	/**
	 * Entity parameter source sql parameter source.
	 *
	 * @param entity the entity
	 * @return the sql parameter source
	 */
	SqlParameterSource entityParameterSource(Object entity);
}
