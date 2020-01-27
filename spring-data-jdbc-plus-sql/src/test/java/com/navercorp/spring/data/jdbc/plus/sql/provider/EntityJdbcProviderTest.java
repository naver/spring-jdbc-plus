package com.navercorp.spring.data.jdbc.plus.sql.provider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.MappingException;

class EntityJdbcProviderTest {
	@Test
	void getRowMapper() {
		EntityQueryMappingConfiguration queryMappingConfiguration = mock(EntityQueryMappingConfiguration.class);
		when(queryMappingConfiguration.getRowMapper(any()))
			.thenThrow(new MappingException("mapping Exception"));

		EntityJdbcProvider sut = new EntityJdbcProvider(null, null, null, queryMappingConfiguration);
		assertThatThrownBy(() -> sut.getRowMapper(Long.class))
			.isInstanceOf(IllegalReturnTypeException.class)
			.hasMessageContaining(Long.class.getName());
	}
}
