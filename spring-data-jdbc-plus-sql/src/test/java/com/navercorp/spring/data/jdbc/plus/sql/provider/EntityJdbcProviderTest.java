/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.sql.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.MappingException;

/**
 * @author Myeonghyeon Lee
 */
class EntityJdbcProviderTest {
	@Test
	void getRowMapper() {
		EntityQueryMappingConfiguration queryMappingConfiguration = mock(EntityQueryMappingConfiguration.class);
		when(queryMappingConfiguration.getRowMapper(any()))
			.thenThrow(new MappingException("mapping Exception"));

		@SuppressWarnings("DataFlowIssue")
		EntityJdbcProvider sut = new EntityJdbcProvider(null, null, null, queryMappingConfiguration, null, null);
		assertThatThrownBy(() -> sut.getRowMapper(Long.class))
			.isInstanceOf(IllegalReturnTypeException.class)
			.hasMessageContaining(Long.class.getName());
	}
}
